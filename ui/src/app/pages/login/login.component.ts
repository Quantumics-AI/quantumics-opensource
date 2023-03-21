import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Quantumfacade } from 'src/app/state/quantum.facade';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  fg: FormGroup;
  rememberMe = false;
  showPassword = false;
  fieldTextType: boolean;

  constructor(
    private fb: FormBuilder,
    private quantumFacade: Quantumfacade) {
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required]
    });

    const email = localStorage.getItem('email');
    if (email) {
      this.rememberMe = true;
      this.fg.controls.email.setValue(email);
    }
  }

  toggleShow() {
    this.showPassword = !this.showPassword;
    this.fieldTextType = !this.fieldTextType;
  }

  login(): void {
    if (this.rememberMe) {
      localStorage.setItem('email', this.fg.value.email);
    } else {
      localStorage.removeItem('email');
    }

    if (this.fg.invalid) {
      return;
    }
    this.quantumFacade.login(this.fg.value);
  }

  public viewPublicWebsite(url: string): void {
    window.open(url, "_blank");
  }
}
