import { Component, OnInit, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-referral',
  templateUrl: './referral.component.html',
  styleUrls: ['./referral.component.scss']
})
export class ReferralComponent implements OnInit {

  isVisibleText: boolean;

  copyInputText(inputElement) {
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);

    this.isVisibleText = true;
    setTimeout(() => this.isVisibleText = false, 2500); // hide the alert after 2.5s
  }

  constructor(
    private router: Router,
  ) { }

  ngOnInit(): void {
  }

}
