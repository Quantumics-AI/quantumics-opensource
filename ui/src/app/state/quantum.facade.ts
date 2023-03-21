import { Injectable } from '@angular/core';
import { QuantumPartialState } from './quantum.reducer';
import { Store, select } from '@ngrx/store';
import { commonQuery } from './quantum.selectors';
import {
  UserLogin, LoadProjects, CertificateLoaded, LoadProjectSource, LoadSourceData, EngineeringLoaded,
  EngineeringLoadedSaveGraph,
  LoadCleaseData, SaveFolder, AddTextOrPattern, AddTextOrPattern1, SaveProject, UploadFile,
  PreviewEngineeringLoaded, LoadPreviewEngineering, LoadEngineering, ResetEngineering, LoadPlans
} from './quantum.actions';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { Certificate } from '../models/certificate';
// import { Folder } from '../source-data/model/folder';
import { GraphParam, CreateGraphParam, PreviewParam } from '../pages/engineering/models/graph-param';
import { Folder } from '../pages/models/folder';
import { Login } from '../pages/login/models/login';

@Injectable({
  providedIn: 'root'
})
export class Quantumfacade {
  loaded$ = this.store.pipe(select(commonQuery.getLoaded));
  certificate$ = this.store.pipe(select(commonQuery.getCertificate));
  projectData$ = this.store.pipe(select(commonQuery.getProjects));
  sourceData$ = this.store.pipe(select(commonQuery.getSourceData));

  projectSource$ = this.store.pipe(select(commonQuery.getProjectSource));
  engineeringData$ = this.store.pipe(select(commonQuery.getEngineeringData));
  previewEngineeringData$ = this.store.pipe(select(commonQuery.getPreviewEngineeringData));
  cleanseData$ = this.store.pipe(select(commonQuery.getCleanseData));
  cleanseDataBK$ = this.store.pipe(select(commonQuery.getCleanseDataBK));
  // textOrPatternData$ = this.store.pipe(select(commonQuery.getTextOrPatternData));
  // convertPatternData$ = this.store.pipe(select(commonQuery.getConvertPatternData));
  // positionsData$ = this.store.pipe(select(commonQuery.getPositionData));


  constructor(private store: Store<QuantumPartialState>) { }
  login(login: Login) {
    this.store.dispatch(new UserLogin(login));
  }
  saveFolder(folder: Folder) {
    this.store.dispatch(new SaveFolder(folder));
  }
  saveProject(project: any) {
    this.store.dispatch(new SaveProject(project));
  }
  uploadFile(file: File, uploadModel: any) {
    this.store.dispatch(new UploadFile({ file, uploadModel }));
  }
  storeCerticate(certificate: Certificate) {
    this.store.dispatch(new CertificateLoaded(certificate));
  }
  loadProjects(projectParam: ProjectParam) {
    this.store.dispatch(new LoadProjects(projectParam));
  }
  loadProjectSource(projectParam: ProjectParam) {
    this.store.dispatch(new LoadProjectSource(projectParam));
  }
  loadEngineeringData(graphParam: GraphParam) {
    this.store.dispatch(new LoadEngineering(graphParam));
  }
  resetEngineeringData() {
    this.store.dispatch(new ResetEngineering());
  }
  loadPreviewEngineeringData(previewParam: PreviewParam) {
    this.store.dispatch(new LoadPreviewEngineering(previewParam));
  }
  saveGraph(graphParam: CreateGraphParam) {
    this.store.dispatch(new EngineeringLoadedSaveGraph(graphParam));
  }
  loadSourceData(projectParam: ProjectParam) {
    this.store.dispatch(new LoadSourceData(projectParam));
  }
  loadCleanseData(projectParam: ProjectParam) {
    this.store.dispatch(new LoadCleaseData(projectParam));
  }
  // addTextOrPattern(addRules: AddRuleParams) {
  //   this.store.dispatch(new AddTextOrPattern(addRules));
  // }
  // addTextOrPattern(textOrPattern: any) {
  //   this.store.dispatch(new AddTextOrPattern(textOrPattern));
  // }
  // addTextOrPattern1(textOrPattern: any) {
  //   this.store.dispatch(new AddTextOrPattern1(textOrPattern));
  // }
  // addConvertPattern(convertPattern: any) {
  //   this.store.dispatch(new AddConvertPattern(convertPattern));
  // }
  // addPositions(position: any) {
  //   this.store.dispatch(new AddPositions(position));
  // }

  loadPlans() {
    this.store.dispatch(new LoadPlans());
  }
}
