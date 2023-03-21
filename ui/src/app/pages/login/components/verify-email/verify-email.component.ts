import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { LoginService } from '../../services/login.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss']
})
export class VerifyEmailComponent implements OnInit {

  private tkn: string;
  constructor(
    private route: ActivatedRoute,
    private loginService: LoginService,
    private snakbar: SnackbarService,
    private router: Router) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.tkn = params['tkn'];

      if (!this.tkn) {
        this.snakbar.open('Invalid link. Please contact to Administrator');
        return;
      }
      else {
        this.loginService.validateEmail(this.tkn).subscribe((response: any) => {
          this.snakbar.open(response.message);
          if (response.code === 200) {
            this.router.navigate(['/login']);
          }
        }, (error) => {
          this.snakbar.open(error);
        });
      }
    });
  }
}
