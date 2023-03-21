import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { LoginService } from '../../services/login.service';

@Component({
  selector: 'app-update-password',
  templateUrl: './update-password.component.html',
  styleUrls: ['./update-password.component.scss']
})
export class UpdatePasswordComponent implements OnInit {
  fg: FormGroup;
  email: string;
  fieldPassType: boolean;
  fieldConfirmPassType: boolean;
  showPass = false;
  showConfirmPass = false;
  constructor(
    private fb: FormBuilder,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private service: LoginService,
    private snackbar: SnackbarService,
  ) {

  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      password: ['', Validators.required],
      confirmPassword: [''],
    }, {
      validator: this.mustMatch('password', 'confirmPassword'),
    });

    this.email = this.activatedRoute.snapshot.paramMap.get('email');
  }

  public resetPassword(): void {
    this.service.resetPassword(this.email, this.fg.value.password).subscribe((res) => {
      this.snackbar.open(res.message);
      this.router.navigate(['/login']);
    });
  }

  private mustMatch(a, b): any {
    return (formGroup: FormGroup) => {
      const control = formGroup.controls[a];
      const matchingControl = formGroup.controls[b];

      if (control.value.length < 8) {
        control.setErrors({ min: true });
        return;
      }

      if (matchingControl.errors && !matchingControl.errors.mustMatch) {
        return;
      }
      if (control.value !== matchingControl.value) {
        matchingControl.setErrors({ mustMatch: true });
      } else {
        matchingControl.setErrors(null);
      }
    };

  }

  togglePass() {
    this.showPass = !this.showPass;
    this.fieldPassType = !this.fieldPassType;
  }

  toggleConfirmPass() {
    this.showConfirmPass = !this.showConfirmPass;
    this.fieldConfirmPassType = !this.fieldConfirmPassType;
  }

}
