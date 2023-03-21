import { createFeatureSelector, createSelector } from '@ngrx/store';
import { QuantumState, quantumFeatureKey } from './quantum.reducer';


const getCommonState = createFeatureSelector<QuantumState>(quantumFeatureKey);

const getLoaded = createSelector(
    getCommonState,
    (state: QuantumState) => state.loaded
);
const getProjects = createSelector(
    getCommonState,
    getLoaded,
    (state: QuantumState, isLoaded) => {
        return isLoaded ? state.projectData : null;
    }
);
const getSourceData = createSelector(
    getCommonState,
    getLoaded,
    (state: QuantumState, isLoaded) => {
        return isLoaded ? state.sourceData : null;
    }
);
const getProjectSource = createSelector(
    getCommonState,
    getLoaded,
    (state: QuantumState, isLoaded) => {
        return isLoaded ? state.projectSource : null;
    }
);
const getEngineeringData = createSelector(
  getCommonState,
  getLoaded,
  (state: QuantumState, isLoaded) => {
    return isLoaded ? state.engineeringData : null;
  }
);
const getPreviewEngineeringData = createSelector(
  getCommonState,
  getLoaded,
  (state: QuantumState, isLoaded) => {
    return isLoaded ? state.previewEngineeringData : null;
  }
);
const getCleanseData = createSelector(
    getCommonState,
    getLoaded,
    (state: QuantumState, isLoaded) => {
        return isLoaded ? state.cleanseData : null;
    }
);
const getCleanseDataBK = createSelector(
  getCommonState,
  getLoaded,
  (state: QuantumState, isLoaded) => {
    return isLoaded ? state.cleanseDataBK : null;
  }
);

// const getConvertPatternData = createSelector(
//     getCommonState,
//     getLoaded,
//     (state: QuantumState, isLoaded) => {
//         return isLoaded ? state.convertPatternData : null;
//     }
// );
// const getPositionData = createSelector(
//     getCommonState,
//     getLoaded,
//     (state: QuantumState, isLoaded) => {
//         return isLoaded ? state.positionData : null;
//     }
// );
const getCertificate = createSelector(
     getCommonState,
     (state: QuantumState) => state.certificate
);
const getError = createSelector(
    getCommonState,
    (state: QuantumState) => state.error

);

export const commonQuery = {
    getLoaded,
    getProjects,
    getCertificate,
    getError,
    getSourceData,
    getEngineeringData,
    getPreviewEngineeringData,
    getProjectSource,
    getCleanseData,
    getCleanseDataBK
};

