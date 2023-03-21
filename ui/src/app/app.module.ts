import { BrowserModule } from '@angular/platform-browser';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ChartsModule } from 'ng2-charts';
import { MatDialogModule } from '@angular/material/dialog';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { EffectsModule } from '@ngrx/effects';
import { NxModule } from '@nrwl/nx';
import { StoreRouterConnectingModule } from '@ngrx/router-store';
import { environment } from '@environment/environment';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatCardModule } from '@angular/material/card';
import { NgSelectModule } from '@ng-select/ng-select';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { quantumFeatureKey, reducer, initialState } from './state/quantum.reducer';
import { NgxSpinnerModule } from 'ngx-spinner';
import { QuantumEffects } from './state/quantum.effects';
import { TokenInterceptor } from './core/interceptors/token.service';
import { CoreModule } from './core/core.module';
import { WorkspaceSettingsComponent } from './pages/workspace-settings/workspace-settings.component';
// import { GtmComponent } from './pages/gtm/gtm.component';

@NgModule({
  declarations: [
    AppComponent,
    WorkspaceSettingsComponent,
    // GtmComponent,
  ],
  imports: [
    CoreModule,
    BrowserModule,
    AppRoutingModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    DragDropModule,
    MatCardModule,
    MatSnackBarModule,
    ChartsModule,
    MatDialogModule,
    NgxSpinnerModule,
    NxModule.forRoot(),
    StoreModule.forRoot({}, {
      runtimeChecks: {
        strictStateImmutability: false,
        strictActionImmutability: false,
      },
    }),
    EffectsModule.forRoot([]),
    StoreModule.forFeature(quantumFeatureKey, reducer, {
      initialState
    }),
    EffectsModule.forFeature([QuantumEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : [],
    StoreRouterConnectingModule.forRoot(),
    NgSelectModule
  ],

  providers: [{
    provide: HTTP_INTERCEPTORS,
    useClass: TokenInterceptor,
    multi: true
  }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  bootstrap: [AppComponent]
})
export class AppModule {

}
