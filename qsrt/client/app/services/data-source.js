import { axios } from "@/services/axios";

export const SCHEMA_NOT_SUPPORTED = 1;
export const SCHEMA_LOAD_ERROR = 2;
export const IMG_ROOT = "/static/images/db-logos";

const DataSource = {
  query: () => axios.get("api/data_sources"),
  get: ({ id }) => axios.get(`api/data_sources/${id}`),
  types: () => axios.get("api/data_sources/types"),
  create: data => axios.post(`api/data_sources`, data),
  save: data => axios.post(`api/data_sources/${data.id}`, data),
  test: data => axios.post(`api/data_sources/${data.id}/test`),
  delete: ({ id }) => axios.delete(`api/data_sources/${id}`),
  getfiles: () => axios.get("login/getfiles"),
  fetchSchema: (data, refresh = false) => {
    const params = {};

    if (refresh) {
      params.refresh = true;
    }

    return axios.get(`api/data_sources/${data.id}/schema`, { params });
  },
  fetchSchemaFromPgsql: (data, projectId, userId, jwt_token, api_url, refresh = false) => {

    const params = {
      method: 'get',
      url: `${api_url}/QuantumSparkServiceAPI/api/v1/redashinfo/files/${projectId}/${userId}/pgsql`,
      headers: {
        'Authorization': `Bearer ${jwt_token}`
      }
    };

    if (refresh) {
      params.refresh = true;
    }

    return axios(params);
  },
};

export default DataSource;
