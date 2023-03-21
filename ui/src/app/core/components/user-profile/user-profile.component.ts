import { Component, EventEmitter, OnInit, Output, ɵɵProvidersFeature } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from '../../services/snackbar.service';
import { UserProfile } from './models/user-profile';
import { UserProfileService } from './services/user-profile.service';
import { CountryService } from './services/country.service';
import { Country } from './models/country';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  @Output() close = new EventEmitter<boolean>();
  private unsubscribe: Subject<void> = new Subject();
  public fg: FormGroup;
  public userId: number;
  public userProfile: UserProfile;
  public countryList: Array<Country>;

  constructor(
    private quantumFacade: Quantumfacade,
    private userProfileService: UserProfileService,
    private countryService: CountryService,
    private snakbar: SnackbarService,
    private fb: FormBuilder) {

    this.quantumFacade.certificate$.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(certificate => {
      this.userId = parseInt(certificate.user_id, 10);
    });
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      firstName: ['', [Validators.required, Validators.required, Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.maxLength(50)]],
      phone: ['', [Validators.required, Validators.maxLength(15), Validators.pattern(/^(?!0+$)(?:\(?\+\d{1,3}\)?[- ]?|0)?\d{10}$/)]],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.maxLength(100)]],
      company: ['', [Validators.required, Validators.maxLength(50)]],
      companyRole: ['', [Validators.required, Validators.maxLength(255)]],
      country: ['', Validators.required],
    });

    this.countryList = this.countryService.getCountryList();
    this.userProfileService.getUserProfile(this.userId).subscribe((profile) => {
      this.userProfile = profile;
      this.userProfile.userImage = profile.userImage?.split('/')?.pop() ?? 'Upload Logo';
      this.setValue(profile);
    });
  }

  private setValue(profile: UserProfile): void {
    this.fg.controls.firstName.setValue(profile.firstName);
    this.fg.controls.lastName.setValue(profile.lastName);
    this.fg.controls.phone.setValue(profile.phone);
    this.fg.controls.email.setValue(profile.email);
    this.fg.controls.company.setValue(profile.company);
    this.fg.controls.companyRole.setValue(profile.companyRole);
    this.fg.controls.country.setValue(profile.country);

    this.fg.markAllAsTouched();
  }

  public validate(event) {

    if (!event.target.value) {
      this.userProfile.userImage = 'Upload Logo';
      return;
    }

    const regex = new RegExp('(.*?).(jpg|png|jpeg)$');
    if (!regex.test(event.target.value.toLowerCase())) {
      event.target.value = '';
      this.snakbar.open('Please select only jpg, png and jpeg file extension');
      return;
    }

    this.userProfile.userImage = event.target?.files[0]?.name;

    if (this.userProfile.userImage.length > 255) {
      event.target.value = '';
      this.userProfile.userImage = 'Upload Logo';
      this.snakbar.open('File name must be less then 255 characters');
      return;
    }

    const file = event.target.files[0];

    if (file?.size > 200000) {
      this.snakbar.open('File should be less than 200KB in size');
      return;
    }

  }

  save(event): void {
    if (this.fg.invalid) {
      return;
    }

    const file = event.files[0];

    if (file?.name > 255) {
      this.userProfile.userImage = 'Upload Logo';
      this.snakbar.open('File name must be less then 255 characters');
      return;
    }

    if (file?.size > 200000) {
      this.snakbar.open('File should be less than 200KB in size');
      return;
    }

    const request = {
      firstName: this.fg.value.firstName,
      lastName: this.fg.value.lastName,
      phone: this.fg.value.phone,
      country: this.fg.value.country,
      company: this.fg.value.company,
      role: this.fg.value.companyRole
    };

    this.userProfileService.updateUserProfile(this.userId, request).subscribe((res) => {
      this.snakbar.open(res.message);
      if (!event.files.length) {
        this.getUserProProfileInfo();
      }
    }, (error) => {
      this.snakbar.open(error);
    });

    if (event.files.length) {
      this.userProfileService.updateUserProfileLogo(this.userId, file).subscribe((res) => {
        this.getUserProProfileInfo();
      });
    }
  }

  private getUserProProfileInfo(): void {
    // update localstorage with updated information.
    this.userProfileService.getUserProfileInfo(this.userId).subscribe((res) => {
      if (res) {
        localStorage.setItem('certificate', JSON.stringify(res));
        this.quantumFacade.storeCerticate(res);
      }
    });
  }
}
