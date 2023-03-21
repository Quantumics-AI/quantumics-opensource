import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfirmationDialogComponent } from 'src/app/core/components/confirmation-dialog/confirmation-dialog.component';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Country } from '../../models/country';
import { UserProfile } from '../../models/user-profile';
import { CountryService } from '../../services/country.service';
import { UserProfileService } from '../../services/user-profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  private unsubscribe: Subject<void> = new Subject();

  public fg: FormGroup;
  public userId: number;
  public userProfile: UserProfile = {} as UserProfile;
  public countryList: Array<Country>;
  public imageUrl: string | ArrayBuffer | null;
  public selectedCountry: string;

  private hasFormChanged: boolean;
  private file: any;

  constructor(
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private fb: FormBuilder,
    private countryService: CountryService,
    private snakbar: SnackbarService,
    private userProfileService: UserProfileService) {
    this.quantumFacade.certificate$.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe(certificate => {
      this.userId = +certificate.user_id;
    });
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      firstName: ['', [Validators.required, Validators.required, Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.maxLength(50)]],
      phone: ['', [Validators.maxLength(15), Validators.pattern(/^(?!0+$)(?:\(?\+\d{1,3}\)?[- ]?|0)?\d{10}$/)]],
      email: [{ value: '', disabled: true }, [ Validators.maxLength(100)]],
      company: ['', [ Validators.maxLength(50)]],
      companyRole: ['', [Validators.maxLength(255)]],
      country: ['',],
    });

    this.countryList = this.countryService.getCountryList();
    this.userProfileService.getUserProfile(this.userId).subscribe((profile) => {
      this.userProfile = profile;
      this.imageUrl = this.userProfile.userImage;
      // this.userProfile.userImage = profile.userImage?.split('/')?.pop() ?? 'Upload Logo';
      this.setValue(profile);
    });


  }

  get f() {
    return this.fg.controls;
  }

  private setValue(profile: UserProfile): void {
    this.f.firstName.setValue(profile.firstName);
    this.f.lastName.setValue(profile.lastName);
    this.f.phone.setValue(profile.phone);
    this.f.email.setValue(profile.email);
    this.f.company.setValue(profile.company);
    this.f.companyRole.setValue(profile.companyRole);
    this.f.country.setValue(profile.country);

    this.fg.markAllAsTouched();

    this.fg.valueChanges.subscribe((data) => {
      this.hasFormChanged = false;
      if (profile.firstName !== data.firstName) {
        this.hasFormChanged = true;
      } else if (profile.lastName !== data.lastName) {
        this.hasFormChanged = true;
      } else if (profile.phone !== data.phone) {
        this.hasFormChanged = true;
      } else if (profile.company !== data.company) {
        this.hasFormChanged = true;
      } else if (profile.companyRole !== data.companyRole) {
        this.hasFormChanged = true;
      } else if (profile.country !== data.country) {
        this.hasFormChanged = true;
      }
    });
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

    this.file = event.target.files[0];

    if (this.file?.size > 200000) {
      this.snakbar.open('File should be less than 200KB in size');
      return;
    }

    const reader = new FileReader();
    reader.readAsDataURL(this.file);

    const certificate: Certificate = JSON.parse(localStorage.getItem('certificate'));

    reader.onload = () => {
      this.hasFormChanged = true;
      this.imageUrl = reader.result;
      certificate.userImageUrl = reader.result.toString();
      this.quantumFacade.storeCerticate(certificate);
    };
  }

  save(event): void {

    this.hasFormChanged = false;
    
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
        const certificate: Certificate = JSON.parse(localStorage.getItem('certificate'));
        res.refreshToken = certificate.refreshToken;
        res.token = certificate.token;

        localStorage.setItem('certificate', JSON.stringify(res));
        this.quantumFacade.storeCerticate(res);
      }
    });
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (!this.hasFormChanged) {
      return true;
    }

    return this.confirmation();
  }

  private confirmation(): Observable<boolean> {
    return new Observable((result) => {
      const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'md' });
      modalRef.componentInstance.title = 'Are you sure?';
      modalRef.componentInstance.message = 'You are going to different screen without saving your changes.';
      modalRef.componentInstance.btnOkText = 'Save & Close';
      modalRef.componentInstance.btnCancelText = 'Discard';
      modalRef.result.then((processWithoutSave) => {

        if (!processWithoutSave) {

          const certificate: Certificate = JSON.parse(localStorage.getItem('certificate'));
          this.imageUrl = certificate.userImageUrl;
          this.quantumFacade.storeCerticate(certificate);

          result.next(true);
          result.complete();
        }
        else {

          if (this.file?.name > 255) {
            this.userProfile.userImage = 'Upload Logo';
            this.snakbar.open('File name must be less then 255 characters');
            result.next(false);
            return;
          }

          if (this.file?.size > 200000) {
            this.snakbar.open('File should be less than 200KB in size');
            result.next(false);
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

            if (!this.file?.name) {
              this.getUserProProfileInfo();
            }
            result.next(true);
            result.complete();

          }, (error) => {
            this.snakbar.open(error);
          });

          if (this.file?.name) {
            this.userProfileService.updateUserProfileLogo(this.userId, this.file).subscribe((res) => {
              const certificate = localStorage.getItem('certificate');

              if (certificate) {
                const certificateJson: Certificate = JSON.parse(certificate);
                certificateJson.userImageUrl = res.body.result;
                localStorage.setItem('certificate', JSON.stringify(certificateJson));
                this.quantumFacade.storeCerticate(certificateJson);
              }

              this.getUserProProfileInfo();
            });
          }
        }
      });
    });
  }
}
