import { Action } from '@ngrx/store';
import { Login } from '../pages/login/models/login';

export enum QuantumActionTypes {
  UserLogin = '[Login] Login',
  CertificateLoaded = '[Certificate Loaded] Certificate Loaded',
  QuantumLoadError = '[Load Error] Error',
  LoadProjects = '[Projects] Load Projects',
  ProjectsLoaded = '[Projects] Projects Loaded',
  LoadSourceData = '[ Source Data] Load Source Data',
  SourceDataLoaded = '[Source Data Loaded] Source Data Loaded',
  LoadProjectSource = '[ Project Source] Load Project Source',
  ProjectSourceLoaded = '[Project Source Loaded] Project Source Loaded',
  EngineeringLoaded = '[Engineering Loaded] Engineering Loaded',
  ResetEngineering = '[Engineering Loaded] Engineering Reset',
  LoadEngineering = '[Load Engineering] Load Engineering',
  LoadPreviewEngineering = '[Preview Engineering] Load Preview Engineering',
  PreviewEngineeringLoaded = '[Preview Engineering Loaded] Prevew Engineering Loaded',
  EngineeringLoadedSaveGraph = '[Engineering Loaded Save Graph] Engineering Loaded Save Graph',
  GraphSaved = '[Engineering Graph Saved] Graph Saved',
  LoadCleaseData = '[Cleanse Data ] Load Cleanse Data',
  CleanseDataLoaded = '[Cleanse Data Loaded] Cleanse Data Loaded',
  CleanseDataLoaded1 = '[Cleanse Data Loaded1] Cleanse Data Loaded1',
  SaveFolder = '[Save Folder] Save Folder',
  SaveProject = '[Save Project] Save Project',
  AddTextOrPattern = '[Add Text or Pattern] Add Text or Pattern',
  AddTextOrPattern1 = '[Add Text or Pattern1] Add Text or Pattern1',
  TextOrPatternLoaded = '[TextOrPatternLoaded] TextOrPatternLoaded',
  TextOrPatternLoaded1 = '[TextOrPatternLoaded1] TextOrPatternLoaded1',
  Redo = '[Redo] Redo',
  UploadFile = '[Upload File] Upload File',
  LoadPlans = '[Plans] Load Plans',
  PlansLoaded = '[Plans] Plans Loaded',
}

export class UserLogin implements Action {
  readonly type = QuantumActionTypes.UserLogin;

  constructor(public payload: Login) {
    console.log(payload);
  }
}
export class QuantumLoadError implements Action {
  readonly type = QuantumActionTypes.QuantumLoadError;
  constructor(public payload: any) { }
}
export class LoadProjects implements Action {
  readonly type = QuantumActionTypes.LoadProjects;
  constructor(public payload: any) { }
}
export class ProjectsLoaded implements Action {
  readonly type = QuantumActionTypes.ProjectsLoaded;
  constructor(public payload: any) { }
}
export class LoadProjectSource implements Action {
  readonly type = QuantumActionTypes.LoadProjectSource;
  constructor(public payload: any) { }
}
export class ProjectSourceLoaded implements Action {
  readonly type = QuantumActionTypes.ProjectSourceLoaded;
  constructor(public payload: any) { }
}

export class LoadEngineering implements Action {
  readonly type = QuantumActionTypes.LoadEngineering;
  constructor(public payload: any) { }
}

export class EngineeringLoaded implements Action {
  readonly type = QuantumActionTypes.EngineeringLoaded;
  constructor(public payload: any) { }
}
export class ResetEngineering implements Action {
  readonly type = QuantumActionTypes.ResetEngineering;
  // constructor(public payload: any) { }
}
export class LoadPreviewEngineering implements Action {
  readonly type = QuantumActionTypes.LoadPreviewEngineering;
  constructor(public payload: any) { }
}
export class PreviewEngineeringLoaded implements Action {
  readonly type = QuantumActionTypes.PreviewEngineeringLoaded;
  constructor(public payload: any) { }
}
export class EngineeringLoadedSaveGraph implements Action {
  readonly type = QuantumActionTypes.EngineeringLoadedSaveGraph;
  constructor(public payload: any) { }
}

export class GraphSaved implements Action {
  readonly type = QuantumActionTypes.GraphSaved;
  constructor(public payload: any) { }
}

export class LoadSourceData implements Action {
  readonly type = QuantumActionTypes.LoadSourceData;
  constructor(public payload: any) { }
}
export class SourceDataLoaded implements Action {
  readonly type = QuantumActionTypes.SourceDataLoaded;
  constructor(public payload: any) { }
}
export class LoadCleaseData implements Action {
  readonly type = QuantumActionTypes.LoadCleaseData;
  constructor(public payload: any) { }
}
export class CleanseDataLoaded implements Action {
  readonly type = QuantumActionTypes.CleanseDataLoaded;
  constructor(public payload: any) { }
}
export class LoadCleaseData1 implements Action {
  readonly type = QuantumActionTypes.LoadCleaseData;
  constructor(public payload: any) { }
}
export class CleanseDataLoaded1 implements Action {
  readonly type = QuantumActionTypes.CleanseDataLoaded1;
  constructor(public payload: any) { }
}
export class CertificateLoaded implements Action {
  readonly type = QuantumActionTypes.CertificateLoaded;
  constructor(public payload: any) { }
}
export class SaveFolder implements Action {
  readonly type = QuantumActionTypes.SaveFolder;
  constructor(public payload: any) { }
}
export class SaveProject implements Action {
  readonly type = QuantumActionTypes.SaveProject;
  constructor(public payload: any) { }
}
export class AddTextOrPattern implements Action {
  readonly type = QuantumActionTypes.AddTextOrPattern;
  constructor(public payload: any) { }
}
export class AddTextOrPattern1 implements Action {
  readonly type = QuantumActionTypes.AddTextOrPattern1;
  constructor(public payload: any) { }
}
export class TextOrPatternLoaded implements Action {
  readonly type = QuantumActionTypes.TextOrPatternLoaded;
  constructor(public payload: any) { }
}
export class TextOrPatternLoaded1 implements Action {
  readonly type = QuantumActionTypes.TextOrPatternLoaded1;
  constructor(public payload: any) { }
}
export class UploadFile implements Action {
  readonly type = QuantumActionTypes.UploadFile;
  constructor(public payload: any) { }
}
export class LoadPlans implements Action {
  readonly type = QuantumActionTypes.LoadPlans;
  constructor() { }
}

export class PlansLoaded implements Action {
  readonly type = QuantumActionTypes.PlansLoaded;
  constructor(public payload: any) { }
}

export type QuantumActions = UserLogin
  | QuantumLoadError
  | LoadProjects
  | ProjectsLoaded
  | CertificateLoaded
  | LoadProjectSource
  | ProjectSourceLoaded
  | EngineeringLoaded
  | ResetEngineering
  | LoadPreviewEngineering
  | PreviewEngineeringLoaded
  | EngineeringLoadedSaveGraph
  | GraphSaved
  | LoadSourceData
  | SourceDataLoaded
  | LoadCleaseData
  | CleanseDataLoaded
  | LoadCleaseData1
  | CleanseDataLoaded1
  | SaveFolder
  | AddTextOrPattern
  | AddTextOrPattern1
  | TextOrPatternLoaded
  | TextOrPatternLoaded1
  | SaveProject
  | UploadFile
  | LoadPlans
  | PlansLoaded;

export const fromQuantumActions = {
  UserLogin,
  QuantumLoadError,
  LoadProjects,
  ProjectsLoaded,
  CertificateLoaded,
  LoadProjectSource,
  ProjectSourceLoaded,
  EngineeringLoaded,
  LoadPreviewEngineering,
  PreviewEngineeringLoaded,
  EngineeringLoadedSaveGraph,
  GraphSaved,
  LoadSourceData,
  SourceDataLoaded,
  LoadCleaseData,
  CleanseDataLoaded,
  LoadCleaseData1,
  CleanseDataLoaded1,
  SaveFolder,
  AddTextOrPattern,
  AddTextOrPattern1,
  TextOrPatternLoaded,
  TextOrPatternLoaded1,
  SaveProject,
  UploadFile,
  LoadPlans,
  PlansLoaded
};
