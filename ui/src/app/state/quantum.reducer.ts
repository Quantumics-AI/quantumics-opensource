import { Action } from '@ngrx/store';
import { QuantumActions, QuantumActionTypes } from './quantum.actions';

import { Certificate } from '../models/certificate';

export const quantumFeatureKey = 'quantum';

export interface QuantumState {
  isLogged: boolean;
  certificate: Certificate;
  error?: any;
  loaded: boolean;
  projectData: any;
  sourceData: any;
  projectSource: any;
  engineeringData: any;
  previewEngineeringData: any;
  cleanseData: any;
  cleanseDataBK: any;
  past: Array<any>;
  present: any;
  future: Array<any>;
  plans: any;
}
export interface QuantumPartialState {
  readonly [quantumFeatureKey]: QuantumState;
}

export const initialState: QuantumState = {
  isLogged: false,
  certificate: null,
  loaded: false,
  projectData: null,
  sourceData: null,
  projectSource: null,
  engineeringData: null,
  previewEngineeringData: null,
  cleanseData: null,
  cleanseDataBK: null,
  past: [],
  present: null,
  future: [],
  plans: null
};

export function reducer(
  state: QuantumState = initialState,
  action: QuantumActions
): QuantumState {
  switch (action.type) {
    case QuantumActionTypes.CertificateLoaded: {
      state = {
        ...state,
        certificate: action.payload,
        isLogged: true,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.UserLogin: {
      state = {
        ...state,
        loaded: false
      };
      break;
    }
    case QuantumActionTypes.SaveFolder: {
      state = {
        ...state,
        loaded: false
      };
      break;
    }
    case QuantumActionTypes.ProjectsLoaded: {
      let payload;
      if (action.payload.message === 'Project created successfully') {
        const s = state.projectData;
        s.result.unshift(action.payload.result);
        payload = s;
      } else {
        payload = action.payload;
      }
      state = {
        ...state,
        projectData: payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.ProjectSourceLoaded: {
      let payload;
      if (action.payload.message === 'Folder created successfully..!') {
        const s = state.projectSource;
        s.result.unshift(action.payload.result);
        payload = s;
      } else {
        payload = action.payload;
      }
      state = {
        ...state,
        projectSource: payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.SourceDataLoaded: {
      state = {
        ...state,
        sourceData: action.payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.EngineeringLoaded: {
      console.log('engineering loaded', action);
      state = {
        ...state,
        engineeringData: action.payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.ResetEngineering: {
      console.log('engineering loaded', action);
      state = {
        ...state,
        engineeringData: null,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.PreviewEngineeringLoaded: {
      console.log('preview engineering loaded', action);
      state = {
        ...state,
        previewEngineeringData: action.payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.CleanseDataLoaded: {
      if (action.payload) {
        state = {
          ...state,
          cleanseData: action.payload,
          cleanseDataBK: state.cleanseDataBK === null ? action.payload : state.cleanseDataBK,
          loaded: true
        };
      } else {
        state = {
          ...state,
          cleanseDataBK: state.cleanseDataBK === null ? action.payload : state.cleanseDataBK,
          loaded: true
        };
      }
      break;
    }
    case QuantumActionTypes.CleanseDataLoaded1: {
      state = {
        ...state,
        cleanseData: action.payload,
        cleanseDataBK: action.payload,
        loaded: true
      };
      break;
    }
    case QuantumActionTypes.PlansLoaded: {
      state = {
        ...state,
        plans: action.payload,
        loaded: true
      };
      break;
    }
  }
  return state;
}
