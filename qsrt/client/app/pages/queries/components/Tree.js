import React, { Component } from 'react';
import values from 'lodash/values';
import PropTypes from 'prop-types';
import DataSource from "./../../../services/data-source";
import TreeNode from './TreeNode';
import { template } from 'lodash';
import axios from 'axios';


const data = {
  "result": [
      {
          "category": "raw",
          "type": "folder",
          "name": "EMPProject",
          "files": [
              {
                  "fileName": "EmpProject2.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.empproject"
              }
          ]
      },
      {
          "category": "raw",
          "type": "folder",
          "name": "EMP",
          "files": [
              {
                  "fileName": "EmpInfo1.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.emp"
              },
              {
                  "fileName": "EmpInfo2.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.emp"
              }
          ]
      },
      {
          "category": "raw",
          "type": "folder",
          "name": "EMP1",
          "files": [
              {
                  "fileName": "EmpInfo2.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.emp1"
              }
          ]
      },
      {
          "category": "raw",
          "type": "folder",
          "name": "EMPDEP",
          "files": [
              {
                  "fileName": "EmpDept2.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.3xso1"
              },
              {
                  "fileName": "EmpDept1.csv",
                  "tableName": "qsai_emptestdatset_rawdb_1599.rdupq"
              }
          ]
      }
  ],
  "code": 200,
  "message": ""
}

const data_hd = {
  '/Source': {
    path: '/Source',
    type: 'folder',
    isRoot: true,
    children: ['/Source/Folder1','/Source/Folder2'],
  },
  '/Source/Folder1': {
    path: '/Source/Folder1',
    type: 'folder',
    isRoot: false,
    children: ['/Source/Folder1/rawdata1.csv','/Source/Folder1/rawdata2.csv'],
  },
  '/Source/Folder1/rawdata1.csv': {
    id: 'abc.table.raw',
    path: '/Source/Folder1/rawdata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Source/Folder1/rawdata2.csv': {
    id: 'abc.table.raw',
    path: '/Source/Folder1/rawdata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Source/Folder2': {
    path: '/Source/Folder2',
    type: 'folder',
    isRoot: false,
    children: ['/Source/Folder2/rawdata1.csv','/Source/Folder2/rawdata2.csv'],
  },
  '/Source/Folder2/rawdata1.csv': {
    id: 'abc.table.raw',
    path: '/Source/Folder2/rawdata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Source/Folder2/rawdata2.csv': {
    id: 'abc.table.raw',
    path: '/Source/Folder2/rawdata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Cleansed': {
    path: '/Cleansed',
    type: 'folder',
    isRoot: true,
    children: ['/Cleansed/Folder1','/Cleansed/Folder2'],
  },
  '/Cleansed/Folder1': {
    path: '/Cleansed/Folder1',
    type: 'folder',
    isRoot: false,
    children: ['/Cleansed/Folder1/cleanseddata1.csv','/Cleansed/Folder1/cleanseddata2.csv'],
  },
  '/Cleansed/Folder1/cleanseddata1.csv': {
    id: 'abc.table.raw',
    path: '/Cleansed/Folder1/cleanseddata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Cleansed/Folder1/cleanseddata2.csv': {
    id: 'abc.table.raw',
    path: '/Cleansed/Folder1/cleanseddata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Cleansed/Folder2': {
    path: '/Cleansed/Folder2',
    type: 'folder',
    isRoot: false,
    children: ['/Cleansed/Folder2/cleanseddata1.csv','/Cleansed/Folder2/cleanseddata2.csv'],
  },
  '/Cleansed/Folder2/cleanseddata1.csv': {
    id: 'abc.table.raw',
    path: '/Cleansed/Folder2/cleanseddata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Cleansed/Folder2/cleanseddata2.csv': {
    id: 'abc.table.raw',
    path: '/Cleansed/Folder2/cleanseddata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Engineered': {
    path: '/Engineered',
    type: 'folder',
    isRoot: true,
    children: ['/Engineered/Flow1','/Engineered/Flow2'],
  },
  '/Engineered/Flow1': {
    path: '/Engineered/Flow1',
    type: 'folder',
    isRoot: false,
    children: ['/Engineered/Flow1/engineeringdata1.csv','/Engineered/Flow1/engineeringdata2.csv'],
  },
  '/Engineered/Flow1/engineeringdata1.csv': {
    id: 'abc.table.raw',
    path: '/Engineered/Flow1/engineeringdata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Engineered/Flow1/engineeringdata2.csv': {
    id: 'abc.table.raw',
    path: '/Engineered/Flow1/engineeringdata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Engineered/Flow2': {
    path: '/Engineered/Flow2',
    type: 'folder',
    isRoot: false,
    children: ['/Engineered/Flow2/engineeringdata1.csv','/Engineered/Flow2/engineeringdata2.csv'],
  },
  '/Engineered/Flow2/engineeringdata1.csv': {
    id: 'abc.table.raw',
    path: '/Engineered/Flow2/engineeringdata1.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },
  '/Engineered/Flow2/engineeringdata2.csv': {
    id: 'abc.table.raw',
    path: '/Engineered/Flow2/engineeringdata2.csv',
    type: 'file',
    content: 'Thanks for reading me me. But there is nothing here.'
  },

};

export default class Tree extends Component {

  state = {
    nodes: data_hd,
  };



  async componentDidMount(){
    let template2 = {
      "folders": [
          {
              "category": "raw",
              "type": "folder",
              "name": "EMPProject",
              "files": [
                  {
                      "fileName": "EmpProject2.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.empproject"
                  }
              ]
          },
          {
              "category": "raw",
              "type": "folder",
              "name": "EMP",
              "files": [
                  {
                      "fileName": "EmpInfo1.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.emp"
                  },
                  {
                      "fileName": "EmpInfo2.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.emp"
                  }
              ]
          },
          {
              "category": "raw",
              "type": "folder",
              "name": "EMP1",
              "files": [
                  {
                      "fileName": "EmpInfo2.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.emp1"
                  }
              ]
          },
          {
              "category": "raw",
              "type": "folder",
              "name": "EMPDEP",
              "files": [
                  {
                      "fileName": "EmpDept2.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.3xso1"
                  },
                  {
                      "fileName": "EmpDept1.csv",
                      "tableName": "qsai_emptestdatset_rawdb_1599.rdupq"
                  }
              ]
          }
      ],
      "code": 200,
      "message": ""
  };



    let master = {'/Source': {
        path: '/Source',
        type: 'folder',
        isRoot: true,
        children: [],
      },
        '/Cleansed': {
        path: '/Cleansed',
        type: 'folder',
        isRoot: true,
        children: [],
      },
        '/Engineered': {
        path: '/Engineered',
        type: 'folder',
        isRoot: true,
        children: [],
      }
    };





const data = await DataSource.getfiles();

const project_id = data.project_id;
const userId = data.userId;
const jwt_token = data.jwt_token;
const api_url = data.api_url;
var config = {
  method: 'get',
  url: `${api_url}/QuantumSparkServiceAPI/api/v1/redashinfo/files/${project_id}/${userId}/aws`,
  headers: {
    'Authorization': `Bearer ${jwt_token}`
  }
};

axios(config)
.then(function (response) {
  let template = response.data.result;
  let dataArray = template["rawFilesResponse"];
  dataArray = dataArray.concat(template["cleansedFilesResponse"]);
  let engineering_dataArray = {};
  if(template["engineeredFilesResponse"].length){
    engineering_dataArray = template["engineeredFilesResponse"];
    for(let k=0; k<engineering_dataArray.length; k++){
      let f_array = engineering_dataArray[k].files;
      for(let h=0; h< f_array.length; h++){
        let file = f_array[h];
        let filePath = `Engineered/${file.fileName}`;
        let fileTemplate ={};
        fileTemplate['path'] = filePath;
        fileTemplate['type'] = 'file';
        var temp = f_array[h].tableName.split('.');
        fileTemplate['id'] = `${temp[0]}."${temp[1]}"`;
        master[filePath] = fileTemplate;
        master['/Engineered'].children.push(filePath);
      }
    }
  }



  for(let i = 0; i<dataArray.length; i++){
    let folder = dataArray[i];
    let key = '';
      switch(folder.category){
        case 'raw':
        {
          key = `/Source/${folder.name}`;
          master['/Source'].children.push(key);
          break;
        }
        case 'processed':
        {
          key = `/Cleansed/${folder.name}`;
          master['/Cleansed'].children.push(key);
          break;
        }
      }

      let folderTemplate = {};
      folderTemplate['path'] = key;
      folderTemplate['isRoot'] = false;
      folderTemplate['type'] = 'folder';
      folderTemplate['children'] = [];
      folderTemplate['external'] = folder.external;
      let files = folder['files'];
      for(let j = 0; j<files.length; j++){
        let file = files[j];
        let filePath = `${key}/${file.fileName}`;
        folderTemplate['children'].push(filePath);
        let fileTemplate ={};
        fileTemplate['path'] = filePath;
        fileTemplate['type'] = 'file';
        fileTemplate['id'] = file.tableName;
        master[filePath] = fileTemplate;
      }
      master[key] = folderTemplate;
  }

})
.catch(function (error) {
  console.log(error);
});




this.setState({
  nodes: master
});

  }



  getRootNodes = () => {
    const { nodes } = this.state;
    return values(nodes).filter(node => node.isRoot === true);
  }

  getChildNodes = (node) => {
    const { nodes } = this.state;
    if (!node.children) return [];
    return node.children.map(path => nodes[path]);
  }

  onToggle = (node) => {
    const { nodes } = this.state;
    nodes[node.path].isOpen = !node.isOpen;
    this.setState({ nodes });
  }

  onNodeSelect = node => {
    const { onSelect } = this.props;
    onSelect(node);
  }

  render() {

    const rootNodes = this.getRootNodes();
    return (
      <div>
        { rootNodes.map(node => (
          <TreeNode
            node={node}
            getChildNodes={this.getChildNodes}
            onToggle={this.onToggle}
            onNodeSelect={this.onNodeSelect}
          />
        ))}
      </div>
    )
  }
}

Tree.propTypes = {
  onSelect: PropTypes.func.isRequired,
};
