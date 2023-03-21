import React from 'react';
import {  FaFolder, FaFolderOpen, FaChevronDown, FaChevronRight, FaDatabase, FaFile, FaEdit, FaCogs, FaFilter } from 'react-icons/fa';
import styled from 'styled-components';
import last from 'lodash/last';
import PropTypes from 'prop-types';

const getPaddingLeft = (level, type) => {
  let paddingLeft = level * 20;
  if (type === 'file') paddingLeft += 20;
  return paddingLeft;
}

const StyledTreeNode = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 5px 8px;
  width: 100vh;
  padding-left: ${props => getPaddingLeft(props.level, props.type)}px;
  &:hover {
    background: lightgray;
  }
`;

const NodeIcon = styled.div`
  font-size: 12px;
  margin-right: ${props => props.marginRight ? props.marginRight : 5}px;
`;

const getNodeLabel = (node) => last(node.path.split('/'));

const TreeNode = (props) => {
  const { node, getChildNodes, level, onToggle, onNodeSelect } = props;

  return (
    <React.Fragment>
      <StyledTreeNode level={level} type={node.type}>
        <NodeIcon onClick={() => onToggle(node)}>
          { node.type === 'folder' && (node.isOpen ? <FaChevronDown /> : <FaChevronRight />) }
        </NodeIcon>
        
        <NodeIcon marginRight={10}>
          { node.type === 'file' && <FaFile /> }
          {/* { node.type === 'folder' && node.isOpen === true && <FaDatabase /> } */}
          {/* { node.type === 'folder' && !node.isOpen && <FaDatabase /> } */}
          { node.type === 'folder'  && node.path === '/Source' &&<FaFile /> }
          { node.type === 'folder'  && node.path === '/Cleansed' &&<FaFilter /> }
          { node.type === 'folder'  && node.path === '/Engineered' &&<FaCogs /> }
          { node.type === 'folder'  && node.path !== '/Cleansed' && node.path !== '/Engineered' &&node.path !== '/Source' && node.external &&<FaDatabase /> }
          { node.type === 'folder'  && node.path !== '/Cleansed' && node.path !== '/Engineered' &&node.path !== '/Source' && !node.external &&<FaFolder /> }

        </NodeIcon>
        

        <span role="button" onClick={() => onNodeSelect(node)}>
          { getNodeLabel(node) }
        </span>
      </StyledTreeNode>

      { node.isOpen && getChildNodes(node).map(childNode => (
        <TreeNode 
          {...props}
          node={childNode}          
          level={level + 1}
        />
      ))}
    </React.Fragment>
  );
}

TreeNode.propTypes = {
  node: PropTypes.object.isRequired,
  getChildNodes: PropTypes.func.isRequired,
  level: PropTypes.number.isRequired,
  onToggle: PropTypes.func.isRequired,
  onNodeSelect: PropTypes.func.isRequired,
};

TreeNode.defaultProps = {
  level: 0,
};

export default TreeNode;
