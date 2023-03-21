import React, { Component } from 'react';
import styled from 'styled-components';
import Tree from './Tree';
import axios from 'axios';

const StyledFileExplorer = styled.div`
  width: 800px;
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  height: calc(100vh - 150px);
  overflow-y: auto;
`;

const TreeWrapper = styled.div`
  width: 250px;
`;

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

}

export default class FileExplorer extends Component { 
  state = {
    selectedFile: null,
    data: null
  };

  componentDidMount(){
    this.setState({
data: data_hd
    });

};

  // getData(){
  //   axios.get('qs url')
  // .then((response) => {
  //   console.log(response.data);
  //   console.log(response.status);
  //   console.log(response.statusText);
  //   console.log(response.headers);
  //   console.log(response.config);
  //   this.setState({
  //     data: response.data
  //   })
  // });
  // }




  onSelect = (file) => {
  if(file.type === 'file'){
    this.setState({ selectedFile: file });
    this.props.onItemSelect(`select * from ${file.id};`);
  }
};

returnTreeView = (data) => {
  return <Tree onSelect={this.onSelect} data={data} />
};
  onItemSelect = this.props.onItemSelect;
  render() {

    const { selectedFile } = this.state;

    return (
      <StyledFileExplorer>
        <TreeWrapper>
          <Tree onSelect={this.onSelect} data={this.state.data} />
          
        </TreeWrapper>

      </StyledFileExplorer>
    )
  }
}