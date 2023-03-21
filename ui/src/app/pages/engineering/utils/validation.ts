import { Configuration } from "../models/configuration";


export const isJoin = (value: string): any => {
    return ['right', 'left', 'full', 'inner'].includes(value);
}

export const isFile = (value: string): any => {
    return ['raw', 'eng', 'processed'].includes(value);
}

export const isValidEdgets = (graph: any, configData: Configuration): any => {
    for (const key in graph) {
        const cell = graph[key];
        const fileData = configData.selectedFiles.find(x => x.eventId === +cell.id);

        if (configData.selectedFiles.length > 1) {
            if (fileData && isFile(fileData.fileType)) {
                if (!cell.edges?.length) {
                    return { message: 'No Edge selected for file', valid: false };
                }
            }
        }
        else if (isJoin(cell?.value)) {
            if (!cell.edges) {
                return { message: 'No Edge between file and Join', valid: false };
            } else if (cell.edges && cell.edges.length == 1) {
                return { message: 'Cannot apply join on Single File', valid: false };
            }
        } else if (cell?.value?.toLowerCase() === 'total') {
            if (!cell.edges?.length) {
                return { message: 'No Edge selected Aggregate', valid: false };
            }
            else if (cell?.edges?.length !== 1) {
                for (const edge in cell?.edges) {
                    const d = cell?.edges[edge];

                    if (!(d.source && d.target)) {
                        return { message: 'No Edge selected Aggregate', valid: false };
                    }
                }
            }
        } else if (cell?.value?.toLowerCase() === 'pgsql') {
            if (!cell.edges?.length) {
                return { message: 'No Edge selected Postgres', valid: false };
            }
            else if (cell?.edges?.length !== 1) {
                for (const edge in cell?.edges) {
                    const d = cell?.edges[edge];

                    if (!(d.source && d.target)) {
                        return { message: 'No Edge selected Postgres', valid: false };
                    }
                }
            }
        }
    }

    return { message: '', valid: true };
}


export const validateJoin = (configData: Configuration): any => {
    const idxForJoin = configData.selectedJoins.findIndex(x => x.eventId1 == null || x.eventId2 == null);
    if (idxForJoin !== -1) {
        return { message: 'Please select join with proper join fields and apply', valid: false };
    }

    return { message: '', valid: true };
}

export const validateAggrigates = (configData: Configuration): any => {
    const idx = configData.selectedAggregates.findIndex(
        x => !x.groupByColumns.length || !x.groupByColumnsMetaData.length
    );

    if (idx !== -1) {
        return { message: 'Please select and save aggrigates fields', valid: false };
    }

    return { message: '', valid: true };

}

export const valiateFileAndJoin = (configData: Configuration): any => {
    configData.selectedJoins.map(t => {
        const idx = configData.selectedFiles.findIndex(x => (x.eventId === t.eventId1) || (x.eventId === t.eventId2));
        if (idx === -1) {
            return { message: 'Cannot Save graph only join field', valid: false };
        }
    });

    configData.selectedFiles.map(t => {
        const idx = configData.selectedJoins.findIndex(x => x.eventId1 === t.eventId || x.eventId2 === t.eventId);
        if (idx === -1) {
            return { message: 'INVALID', valid: false };
        }
    });

    return { message: '', valid: true };

}


export const validate = (graph: any, configData: Configuration): any => {
    const validation1 = isValidEdgets(graph, configData)
    if (!validation1.valid) {
        return validation1;
    }

    const validation2 = validateJoin(configData);
    if (!validation2.valid) {
        return validation2;
    }

    const validation3 = validateAggrigates(configData);
    if (!validation3.valid) {
        return validation2;
    }

    const validation4 = valiateFileAndJoin(configData);
    if (validation4.valid) {
        return validation4;
    }
}
