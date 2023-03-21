import { Injectable } from '@angular/core';
import { Actions, createEffect, act, ofType } from '@ngrx/effects';
import { QuantumPartialState } from './quantum.reducer';
import { DataPersistence } from '@nrwl/nx';
import {
  QuantumActionTypes,
  UserLogin,
  CertificateLoaded,
  QuantumLoadError,
  LoadProjects,
  ProjectsLoaded,
  LoadProjectSource,
  ProjectSourceLoaded,
  EngineeringLoaded,
  EngineeringLoadedSaveGraph,
  LoadSourceData,
  SourceDataLoaded,
  LoadCleaseData,
  CleanseDataLoaded,
  CleanseDataLoaded1,
  SaveFolder,
  AddTextOrPattern,
  AddTextOrPattern1,
  SaveProject,
  UploadFile, PreviewEngineeringLoaded, LoadPreviewEngineering, LoadEngineering, GraphSaved, LoadPlans, PlansLoaded
} from './quantum.actions';

import { from, of } from 'rxjs';
import { map, withLatestFrom } from 'rxjs/operators';
import { ProjectsService } from '../pages/projects/services/projects.service';
import { SourceDataService } from '../pages/source-data/services/source-data.service';
import { EngineeringService } from '../pages/engineering/services/engineering.service';
import { Router } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { SnackbarService } from '../core/services/snackbar.service';
import { LoginService } from '../pages/login/services/login.service';
import { PlansService } from '../pages/projects/services/plans.service';
import { environment } from '../../environments/environment';

declare let window: any;

@Injectable()
export class QuantumEffects {

  constructor(
    private store: Store<QuantumPartialState>,
    private actions$: Actions,
    private dataPersistence: DataPersistence<QuantumPartialState>,
    private loginService: LoginService,
    private projectService: ProjectsService,
    private plansService: PlansService,
    private sourceDataService: SourceDataService,
    private engineeringService: EngineeringService,
    private snackBar: SnackbarService,
    private router: Router) { }

   login$ = createEffect(() => this.dataPersistence.fetch(QuantumActionTypes.UserLogin,
    {
      run: (action: UserLogin, state: QuantumPartialState) => {
        return from(this.loginService.login(action.payload)).pipe(map
          ((response: any) => {

              this.router.navigate(['/projects']);
              this.snackBar.open(response.message);
            return new CertificateLoaded(response);
          }));
      },
      onError: (action: UserLogin, error: any) => {
        this.snackBar.open(error);
        return new QuantumLoadError(error);
      }
    }
  ));
   saveFolder$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.SaveFolder,
    {
      run: (action: SaveFolder, state: QuantumPartialState) => {
        return from(this.sourceDataService.createFolder(action.payload)).pipe(map
          ((response: any) => {
            this.snackBar.open(response.message);
            return new ProjectSourceLoaded(response);
          }));
      },
      onError: (action: SaveFolder, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
   saveProject$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.SaveProject,
    {
      run: (action: SaveProject, state: QuantumPartialState) => {
        return from(this.projectService.createProject(action.payload))
          .pipe(map((response: any) => {
            this.snackBar.open(response.message);
            return new ProjectsLoaded(response);
          }));
      },
      onError: (action: SaveProject, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
   uploadFile$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.UploadFile,
    {
      run: (action: UploadFile, state: QuantumPartialState) => {
        console.log(action.payload);
        return from(this.sourceDataService.upload(action.payload.file, action.payload.uploadModel)).pipe(map
          ((response: any) => {
            console.log(response);
            if (response) {
              alert('Your file is being uploaded, it will appear in your directory in sometime');
              // this.snackBar.open(
              // 'Your file is being uploaded, it will appear in your directory in sometime', 'Ok', -1);
            }
            return new SourceDataLoaded(response);
          }));
      },
      onError: (action: UploadFile, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
   loadProjectData$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadProjects,
    {
      run: (action: LoadProjects, state: QuantumPartialState) => {
        return from(this.projectService.getProjects(action.payload)).pipe(map
          (response => {
            if (response) {
              // this.snackBar.open(response.message);

            } else {
              this.snackBar.open(response.message);
            }
            return new ProjectsLoaded(response);
          }));
      },
      onError: (action: LoadProjects, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   loadProjectSource$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadProjectSource,
    {
      run: (action: LoadProjectSource, state: QuantumPartialState) => {
        return from(this.sourceDataService.getFolders(action.payload)).pipe(map
          (response => {
            if (response) {
              // this.snackBar.open(response.message);

            } else {
              this.snackBar.open(response.message);
            }
            return new ProjectSourceLoaded(response);
          }));
      },
      onError: (action: LoadProjectSource, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   loadEngineering$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadEngineering,
    {
      run: (action: LoadEngineering, state: QuantumPartialState) => {
        return from(this.engineeringService.getSavedGraph(action.payload)).pipe(map
          (response => {
            if (response) {
              // this.snackBar.open(response.message);
            } else {
              this.snackBar.open(response.message);
            }
            return new EngineeringLoaded(response);
          }));
      },
      onError: (action: LoadEngineering, error) => {
        console.log('Error on loadEngineering ', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   loadPreviewEngineering$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadPreviewEngineering,
    {
      run: (action: LoadPreviewEngineering, state: QuantumPartialState) => {
        return from(this.engineeringService.getPreviewContent(action.payload)).pipe(map
          ((response: any) => {
            if (response) {
              // this.snackBar.open(response.message);
            } else {
              this.snackBar.open(response.message);
            }
            return new PreviewEngineeringLoaded(response);
          }));
      },
      onError: (action: LoadPreviewEngineering, error) => {
        console.log('Error on previewLoadEngineering ', error);
        return new QuantumLoadError(error);
      }
    }
  ));


   loadEngineeringSaveGraph$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.EngineeringLoadedSaveGraph,
    {
      run: (action: EngineeringLoadedSaveGraph, state: QuantumPartialState) => {
        console.log(action.payload);
        return from(this.engineeringService.saveGraph(action.payload)).pipe(map
          ((response: any) => {
            this.snackBar.open(response.message);
            return new GraphSaved(response);
          }));
      },
      onError: (action: EngineeringLoadedSaveGraph, error) => {
        console.log('Error on EngineeringLoadedSaveGraph', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   loadSourceData$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadSourceData,
    {
      run: (action: LoadSourceData, state: QuantumPartialState) => {
        return from(this.sourceDataService.getFiles(action.payload)).pipe(map
          (response => {
            if (response) {
              // this.snackBar.open(response.message);
            } else {
              this.snackBar.open(response.message);
            }
            return new SourceDataLoaded(response);
          }));
      },
      onError: (action: LoadSourceData, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
   loadCleanseData$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadCleaseData,
    {
      run: (action: LoadCleaseData, state: QuantumPartialState) => {
        return from(this.sourceDataService.getFileContent(action.payload)).pipe(map
          (response => {
            if (response) {
              // this.snackBar.open(response.message);
            } else {
              this.snackBar.open(response.message);
            }
            return new CleanseDataLoaded(response);
          }));
      },
      onError: (action: LoadCleaseData, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   addTextOrPatternData$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.AddTextOrPattern,
    {
      run: (action: AddTextOrPattern, state: QuantumPartialState) => {
        return new CleanseDataLoaded(action.payload);

      },
      onError: (action: AddTextOrPattern, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
   addTextOrPatternData1$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.AddTextOrPattern1,
    {
      run: (action: AddTextOrPattern1, state: QuantumPartialState) => {
        return new CleanseDataLoaded1(action.payload);

      },
      onError: (action: AddTextOrPattern1, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));

   loadPlans$ = createEffect(() => this.dataPersistence.fetch(
    QuantumActionTypes.LoadPlans,
    {
      run: (action: LoadPlans, state: QuantumPartialState) => {
        return from(this.plansService.getPlans())
          .pipe(map((response: any) => {
            return new PlansLoaded(response);
          }));
      },
      onError: (action: LoadPlans, error) => {
        console.log('Error', error);
        return new QuantumLoadError(error);
      }
    }
  ));
}
