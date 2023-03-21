import {
  Component,
  OnInit,
  AfterViewInit,
  ElementRef,
  ViewChild,
  OnDestroy
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Observable, Subject, Subscription, timer } from 'rxjs';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { EngineeringService } from '../../services/engineering.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

import { Configuration, PreviewContent, SelectedFile, SelectedJoin, SelectedUdf } from '../../models/configuration';
import { validate } from '../../utils/validation';
import { EngFileImages, FileImages } from '../../constants/file-images';
import { FullJoinImages, InnerJoinImages, LeftJoinImages, RightJoinImages } from '../../constants/join-images';
import { UdfImages } from '../../constants/udf-images'
import { PreviewParam } from '../../models/graph-param';
import { AggregateImages } from '../../constants/aggregate-images';

declare var mxGraph: any;
declare var mxGraphHandler: any;
declare var mxKeyHandler: any;
declare var mxEvent: any;
declare var mxUtils: any;
declare var mxRubberband: any;
declare var mxPolyline: any;
declare var mxShape: any;
declare var mxConnectionConstraint: any;
declare var mxPoint: any;
declare var mxClient: any;
declare var mxCodec: any;
declare var mxVertexHandler: any;
declare var mxMultiplicity: any;
declare var mxConstants: any;
declare var mxConstraintHandler: any;
declare var mxImage: any;

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.scss']
})
export class EditComponent implements OnInit, OnDestroy, AfterViewInit {
  selectedEventId: number;
  viewOnly = false;
  public userId: number;

  @ViewChild('graphContainer') graphContainer: ElementRef;
  private unsubscribe: Subject<void> = new Subject();
  private subscription: Subscription;
  graph: any;
  joins = ['right', 'left', 'full', 'inner'];
  public projectId: any;
  folderId;
  file;
  joinColumn: any[] = [];
  engineeringData$: Observable<any>;
  loaded$: Observable<boolean>;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  projectParam: ProjectParam;
  folderName: any;
  joinsArray = [];
  joinsCount = 0;
  isPreview = false;
  configData: Configuration;
  tempConfigData: Configuration;
  flowId: number;
  loading: boolean;
  projectName: string;
  isPreviewNew = false;
  selectedJoin: SelectedJoin;
  selectedFile: SelectedFile;
  selectedUdf: SelectedUdf;
  selectedAggregate: any;
  eventId: number;
  flowName: string;
  flowViewName: string;
  flowViewUrl: string;
  folders = [];
  items2 = [];
  everyFiveSeconds: Observable<number> = timer(0, 5000);
  graphProgressPercentage = 10;
  public df: any;
  public eventIdData: {};
  public event_id_1: number;
  public event_id_2: number;
  public event_id_3: number;
  public event_id_4: number;
  public event_id_5: number;
  public udf_syntax: string;
  public udf_arguments: any;
  public udf_type_name: string;
  public udf_id: number;
  public configEventId: number;
  public storedColumns = [] as any;
  public elements: Array<any> = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private engineeringService: EngineeringService,
    private snackbarService: SnackbarService,
    private router: Router,
  ) {
    this.engineeringData$ = this.quantumFacade.engineeringData$;
    this.loaded$ = this.quantumFacade.loaded$;
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +this.certificateData.user_id;
      });

    this.projectName = localStorage.getItem('projectname');
  }

  ngAfterViewInit(): void {

    mxGraphHandler.prototype.guidesEnabled = true;
    // Overridden to define per-shape connection points
    mxGraph.prototype.getAllConnectionConstraints = (terminal, source) => {
      if (terminal != null && terminal.shape != null) {
        if (terminal.shape.stencil != null) {
          if (terminal.shape.stencil.constraints != null) {
            return terminal.shape.stencil.constraints;
          }
        } else if (terminal.shape.constraints != null) {
          return terminal.shape.constraints;
        }
      }
      return null;
    };

    // Images for selected highlight areas only.
    mxConstraintHandler.prototype.pointImage = new mxImage('/assets/img/dot.gif', 10, 10);

    // Defines the default constraints for all shapes
    mxShape.prototype.constraints = [
      new mxConnectionConstraint(new mxPoint(0.5, 0), true),
      new mxConnectionConstraint(new mxPoint(0, 0.5), true),
      new mxConnectionConstraint(new mxPoint(1, 0.5), true),
      new mxConnectionConstraint(new mxPoint(0.5, 1), true),
    ];

    mxConstants.DEFAULT_VALID_COLOR = '#2196f3';
    mxConstants.HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.TARGET_HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.INVALID_CONNECT_TARGET_COLOR = '#2196f3';
    mxConstants.DROP_TARGET_COLOR = '#2196f3';

    mxConstants.VALID_COLOR = '#2196f3';
    mxConstants.INVALID_COLOR = '#2196f3';
    mxConstants.OUTLINE_HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.CONNECT_HANDLE_FILLCOLOR = '#2196f3';
    mxConstants.OUTLINE_COLOR = '#2196f3';
    mxConstants.OUTLINE_HANDLE_FILLCOLOR = '#2196f3';

    mxConstants.SHADOWCOLOR = '#2196f3';
    mxConstants.VML_SHADOWCOLOR = '#2196f3';
    mxConstants.DEFAULT_VALID_COLOR = '#ffffff';
    mxConstants.DEFAULT_INVALID_COLOR = '#2196f3';

    mxConstants.LABEL_HANDLE_FILLCOLOR = '#2196f3';
    mxConstants.LOCKED_HANDLE_FILLCOLOR = '#2196f3';
    mxConstants.HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.HIGHLIGHT_COLOR = '#2196f3';
    mxConstants.HIGHLIGHT_COLOR = '#2196f3';
    mxConstraintHandler.prototype.highlightColor = mxConstants.DEFAULT_VALID_COLOR;

    // Edges have no connection points
    mxPolyline.prototype.constraints = null;

    // Program starts here. Creates a sample graph in the
    // DOM node with the specified ID. This function is invoked
    // from the onLoad event handler of the document (see below).
    const main = container => {
      // Checks if the browser is supported
      if (!mxClient.isBrowserSupported()) {
        // Displays an error message if the browser is not supported.
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        // Disables the built-in context menu
        mxEvent.disableContextMenu(container);

        // Creates the graph inside the given container
        this.graph = new mxGraph(container);
        // const keyHandler = new mxKeyHandler(this.graph);

        this.graph.setConnectable(true);

        // Specifies the default edge style
        this.graph.getStylesheet().getDefaultEdgeStyle().edgeStyle =
          'orthogonalEdgeStyle';


        this.graph.setPanning(true);
        this.graph.setTooltips(true);

        this.graph.getLabel = (cell) => {
          return '';
        };

        // Enables rubberband selection
        // tslint:disable-next-line:no-unused-expression
        new mxRubberband(this.graph);

        const self = this;
        const listener = async (sender, evt) => {
          self.graph.validateGraph();
          self.customValidate();
          for (const key in sender.cells) {
            if (sender.cells.hasOwnProperty(key)) {
              const val = sender.cells[key];
              if (val.isVertex() &&
                val.getEdgeCount() === 2 &&
                val.mxObjectId === self.joinsArray[self.joinsArray.length - 1]) {
              }
            }
          }
        };

        this.graph.addListener(mxEvent.CLICK, async (sender, evt) => {
          // set focus to graph when user come back to preview view.
          if (!(self.isPreview || self.viewOnly)) {
            this.graph.container.setAttribute('tabindex', '-1');
            this.graph.container.style.outline = 'none';
            this.graph.container.focus();
          }

          const cell = evt.properties?.cell;
          this.selectedEventId = +cell?.id;

          setTimeout(() => {

            const selectedCell = self.graph.getSelectionCell();

            if (selectedCell) {
              this.eventId = +selectedCell?.id;
            }

            // First check for join
            let isJoin = this.isJoin(selectedCell);

            if (!isJoin) {
              const hasTargetJoinSelected = this.joins.includes(selectedCell?.target?.value);

              if (hasTargetJoinSelected) {
                if (selectedCell.target.edges.length == 2) {
                  isJoin = true;
                }
              }
            }

            if (isJoin) {
              this.selectedFile = null;
              this.selectedAggregate = null;
              this.selectedUdf = null;
              this.selectedJoin = this.configData.selectedJoins.find(x => x.autoConfigEventId === +selectedCell?.id);

              if (!this.selectedJoin) {
                this.selectedJoin = this.configData.selectedJoins.find(x => x.autoConfigEventId === +selectedCell?.target?.id);
              }

              if (this.selectedJoin) {
                const edges = self.graph.getIncomingEdges(cell);

                if (edges.length === 2) {
                  const joinFolders = [];
                  for (const edge of edges) {
                    const file = this.configData.selectedFiles.find(x => x.eventId === +edge.source.id);

                    // if file is empty then check for join
                    if (!file) {
                      const join = this.configData.selectedJoins.find(x => x.eventId === +edge.source.id);

                      if (join?.metaData) {
                        joinFolders.push({ columns: join?.metaData, file: join.joinName });
                      } else {
                        const udf = this.configData.selectedUdfs.find(x => x.autoConfigEventId === +edge.source.id);

                        if (udf.metaData) {
                          joinFolders.push({ columns: udf?.metaData, file: udf.fileType });
                        }
                      }
                    } else {
                      joinFolders.push({ columns: file?.metaData, file: file.name });
                    }
                  }

                  this.selectedJoin.joinFolders = joinFolders;
                  this.isPreviewNew = true;
                } else {
                  this.isPreviewNew = false;
                }
              }
            } else if (cell?.value?.toLowerCase() === 'total') {
              const edges = self.graph.getIncomingEdges(cell);
              if (edges.length === 1) {
                this.selectedJoin = null;
                this.selectedFile = null;
                this.selectedUdf = null;

                const edge = edges[0];
                const evtId = +edge.source.id;
                const allEvents = [...this.configData.selectedFiles, ...this.configData.selectedJoins, ...this.configData.selectedUdfs];
                const currentItem = allEvents.find(f => f.autoConfigEventId === evtId);
                const selectedAggregate = this.configData.selectedAggregates.find(a => a.autoConfigEventId === this.selectedEventId);
                selectedAggregate.columns = currentItem?.metaData;
                selectedAggregate.dataFrameEventId = +edge.source.id;
                this.selectedAggregate = selectedAggregate;
                this.isPreviewNew = true;
              } else {
                this.isPreviewNew = false;
              }
            } else if (cell?.value?.toLowerCase() === 'pgsql') {
              this.isPreviewNew = false;
            } else {
              const file = this.configData.selectedFiles.find(x => x.autoConfigEventId === +selectedCell?.id);
              this.selectedUdf = this.configData.selectedUdfs.find(x => x.autoConfigEventId === +selectedCell?.id);

              if (!this.selectedUdf) {
                this.selectedUdf = this.configData.selectedUdfs.find(x => x.autoConfigEventId === +selectedCell?.target?.id);
              }

              if (this.selectedUdf != undefined) {
                this.selectedJoin = null;
                this.selectedFile = null;
                this.selectedAggregate = null;

                const edges = self.graph.getIncomingEdges(cell);

                if (edges.length !== 0 && edges.length <= 5) {
                  const joinFolders = [];
                  for (const edge of edges) {
                    const file = this.configData.selectedFiles.find(x => x.eventId === +edge.source.id);

                    if (file != undefined) {
                      if (!file) {
                        const file_name = this.configData.selectedUdfs.find(x => x.eventId === +edge.source.id);

                        if (file_name.metaData) {
                          joinFolders.push({ file: file_name.joinName });
                        }
                      } else {
                        joinFolders.push({ columns: file?.metaData, file: file.name, id: file.fileId, eventId: file.eventId });
                      }
                    } else {
                      const file = this.configData.selectedJoins.find(x => x.eventId === +edge.source.id);
                      if (file != undefined) {
                        if (!file) {
                          const file_name = this.configData.selectedUdfs.find(x => x.eventId === +edge.source.id);

                          if (file_name.metaData) {
                            joinFolders.push({ file: file_name.joinName });
                          }
                        } else {
                          joinFolders.push({ columns: file?.metaData, file: '' });
                        }
                      } else {
                        const file = this.configData.selectedUdfs.find(x => x.autoConfigEventId === +edge.source.id);

                        if (!file) {
                          const file_name = this.configData.selectedUdfs.find(x => x.eventId === +edge.source.id);

                          if (file_name.metaData) {
                            joinFolders.push({ file: file_name.joinName });
                          }
                        } else {
                          joinFolders.push({ columns: file?.metaData, file: file.joinName, id: file.fileId, eventId: file.eventId });
                        }
                      }
                    }
                  }

                  this.selectedUdf.joinFolders = joinFolders;
                  this.isPreviewNew = true;
                } else {
                  this.isPreviewNew = false;
                }
              }

              if (file?.type?.toLowerCase() === 'file' || file?.fileType.toLowerCase()?.includes('raw')) {
                this.selectedJoin = null;
                this.selectedAggregate = null;
                this.isPreviewNew = true;
                this.selectedFile = file;
                this.selectedUdf = null;
              }
            }
          }, 10);
        });

        this.graph.getModel().addListener(mxEvent.CHANGE, listener);

        // const parent = this.graph.getDefaultParent();

        // Adds cells to the model in a single step
        this.graph.getModel().beginUpdate();
        try {
        } finally {
          // Updates the display
          this.graph.getModel().endUpdate();
        }

        function mxVertexToolHandler(state) {
          mxVertexHandler.apply(this, arguments);
        }

        mxVertexToolHandler.prototype = new mxVertexHandler();
        mxVertexToolHandler.prototype.constructor = mxVertexToolHandler;

        mxVertexToolHandler.prototype.domNode = null;

        mxVertexToolHandler.prototype.init = function () {
          mxVertexHandler.prototype.init.apply(this, arguments);

          // In this example we force the use of DIVs for images in IE. This
          // handles transparency in PNG images properly in IE and fixes the
          // problem that IE routes all mouse events for a gesture via the
          // initial IMG node, which means the target vertices
          this.domNode = document.createElement('div');
          this.domNode.style.position = 'absolute';
          this.domNode.style.whiteSpace = 'nowrap';
        };

        mxVertexToolHandler.prototype.redraw = function () {
          mxVertexHandler.prototype.redraw.apply(this);
          this.redrawTools();
        };

        mxVertexToolHandler.prototype.redrawTools = function () {
          if (this.state != null && this.domNode != null) {
            const dy =
              mxClient.IS_VML && document.compatMode === 'CSS1Compat' ? 20 : 4;
            this.domNode.style.left =
              this.state.x + this.state.width - 56 + 'px';
            this.domNode.style.top =
              this.state.y + this.state.height + dy + 'px';
          }
        };

        mxVertexToolHandler.prototype.destroy = function (sender, me) {
          mxVertexHandler.prototype.destroy.apply(this, arguments);

          if (this.domNode != null) {
            this.domNode.parentNode?.removeChild(this.domNode);
            this.domNode = null;
          }
        };
      }


      const myClass = this;

      const deleteElement = function () {
        myClass.deleteElement();
      }

      const nudge = function (keyCode) {
        if (!myClass.graph.isSelectionEmpty()) {
          let dx = 0;
          let dy = 0;
          if (keyCode === 37) {
            dx = -1;
          } else if (keyCode === 38) {
            dy = -1;
          } else if (keyCode === 39) {
            dx = 1;
          } else if (keyCode === 40) {
            dy = 1;
          }

          myClass.graph.moveCells(myClass.graph.getSelectionCells(), dx, dy);
        }
      };

      // Transfer initial focus to graph container for keystroke handling
      this.graph.container.focus();

      // Handles keystroke events
      const keyHandler = new mxKeyHandler(this.graph);

      // Ignores enter keystroke. Remove this line if you want the
      // enter keystroke to stop editing
      keyHandler.enter = function () { };

      // Remove selected elements using keyboard delete button. 
      keyHandler.bindKey(46, function (event) {
        deleteElement();
      });

      keyHandler.bindKey(37, function () {
        nudge(37);
      });

      keyHandler.bindKey(38, () => {
        nudge(38);
      });

      keyHandler.bindKey(39, () => {
        nudge(39);
      });

      keyHandler.bindKey(40, () => {
        nudge(40);
      });

    };

    main(document.getElementById('graphContainer'));
    // 	toolbar.enabled = false
  }

  ngOnInit(): void {
    this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.flowId = +this.activatedRoute.snapshot.paramMap.get('flowId');
    this.flowName = this.activatedRoute.snapshot.paramMap.get('flowName');
    this.flowViewName = this.flowName.replace(/ /g, '_');
    this.flowViewUrl = this.router.url.replace(/%20/g, '_');
    const url: string = this.flowViewUrl.substring(0, (this.flowViewUrl.length - this.flowViewName.length - 1));
    this.isPreview = this.viewOnly = url.endsWith('view');

    this.loadGraph();
  }

  private loadGraph(): void {
    this.loading = true;
    const params = {
      userId: this.certificateData.user_id,
      projectId: this.projectId,
      flowId: this.flowId
    };

    // this.quantumFacade.loadEngineeringData(params);

    this.configData = {
      projectId: +this.projectId,
      userId: +this.certificateData.user_id,
      engineeringId: this.flowId,
      selectedFiles: [],
      selectedJoins: [],
      selectedAggregates: [],
      selectedUdfs: []
    };

    const sub = this.everyFiveSeconds.subscribe(() => {
      if (this.graphProgressPercentage < 90) {
        this.graphProgressPercentage += 7;
      } else {
        this.graphProgressPercentage = 99;
      }
    });

    this.engineeringService.getSavedGraph(params)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((data) => {
        if (data.code !== 200) {
          this.loading = false;
        }
        else if (data.result) {
          let doc;
          doc = mxUtils.parseXml(JSON.parse(data.result.graph));
          const codec = new mxCodec(doc);
          const elt = doc.documentElement.firstChild;
          codec.decode(doc.documentElement, this.graph.getModel());
          const t = JSON.parse(data.result.config);
          this.configData = Object.assign(this.configData, t);

          this.configData.selectedAggregates.map(t => {
            this.elements.push(t.autoConfigEventId);
          });

          this.configData.selectedJoins.map(t => {
            this.elements.push(t.autoConfigEventId);
          });

          this.configData.selectedFiles.map(t => {
            this.elements.push(t.autoConfigEventId);
          });

          this.configData.selectedUdfs.map(t => {
            this.elements.push(t.autoConfigEventId);
          });

          this.loading = false;

          if (sub) {
            sub.unsubscribe();
          }

          this.intervalCall();
        }
      }, () => {
        this.loading = false;
      });
  }

  private intervalCall(): void {

    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    const source = interval(3000);
    this.subscription = source.subscribe(() => this.getPreviewContent());
  }

  customValidate() {
    const cells = this.graph.getModel().cells;

    for (const key in cells) {
      if (!cells.hasOwnProperty(key)) { continue; }

      // tslint:disable-next-line:no-shadowed-variable
      const mxCell = cells[key];
      if (!mxCell.isVertex() && !mxCell.isEdge()) { continue; }

      let notConnected = true;
      if (mxCell.isVertex()) {
        for (let i = 0; i < mxCell.getEdgeCount(); i++) {
          const edge = mxCell.getEdgeAt(i);
          if (edge.source !== null && edge.target !== null) {
            notConnected = false;
            break;
          }
        }
      } else {
        notConnected = mxCell.source === null || mxCell.target === null;
      }
    }
  }

  saveGraph(byPassValidation = false) {
    const graph = this.graph.getModel().cells;

    if (!byPassValidation) {
      const validated = validate(graph, this.configData);

      if (!validated.valid) {
        this.snackbarService.open(validated.message);
        return;
      }
    }

    // get config data into temp and then remove content property.
    this.tempConfigData = JSON.parse(JSON.stringify(this.configData));

    // remove the extra added content propery from configuration
    this.tempConfigData.selectedFiles.map(t => delete t.content);
    this.tempConfigData.selectedJoins.map(t => delete t.content);
    this.tempConfigData.selectedAggregates.map(t => delete t.content);
    this.tempConfigData.selectedAggregates.map(t => {
      t.columns = t.columnsMetaData;
    });
    this.tempConfigData.selectedUdfs.map(t => delete t.content);

    const encoder = new mxCodec();
    const node = encoder.encode(this.graph.getModel());

    const payload = {
      content: JSON.stringify(mxUtils.getPrettyXml(node)),
      config: JSON.stringify(this.tempConfigData),
      projectId: Number(this.projectId),
      userId: Number(this.certificateData.user_id),
      engFlowId: this.flowId
    };

    this.engineeringService.saveGraph(payload).subscribe((response) => {
      if (response.code === 201) {
        this.snackbarService.open(response.message);
      }
    });
  }

  addTotalElement(name = 'TOTAL') {
    this.joinsCount = this.joinsCount + 1;
    setTimeout(() => {
      if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();

        try {

          this.graph.multiplicities.push(new mxMultiplicity(
            false, name, null, null, 0, 1, ['RO', 'LO'],
            'Aggregation can have only one Outgoing node',
            null));

          this.graph.multiplicities.push(new mxMultiplicity(
            true, name, null, null, 0, 1, ['RO', 'LO'],
            'Aggregation can have only one Outgoing node',
            null));

          const yPosition = this.getYPostion(this.graph?.model?.cells, 'total');
          const img = '/assets/images/aggregate.svg';
          const vertex = this.graph.insertVertex(parent, null, name, 400, yPosition, 80, 80, `shape=image;image=${img};resizable=0`);

          const d = {
            projectId: this.projectId,
            type: 'agg',
            engFlowId: this.flowId
          };

          this.engineeringService.createAggregateEvent(d).subscribe((res) => {
            vertex.id = res.result;
            this.elements.push(res.result);
            this.configData.selectedAggregates.push({
              engFlowId: this.flowId,
              projectId: this.projectId,
              dataFrameEventId: null,
              eventId: res.result,
              autoConfigEventId: res.result,
              metaData: undefined,
              groupByColumns: [],
              columns: []
            });
          });
        } finally {
          // Updates the display
          this.graph.getModel().endUpdate();
        }
      }
    }, 200);
  }

  udfElement() {
    setTimeout(() => {
      if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        try {
          const img = '/assets/images/udf.png';
          this.graph.insertVertex(parent, null, 'udf', 400, 100, 80, 80, `shape=image;image=${img};resizable=0`);

        } finally {
          // Updates the display
          this.graph.getModel().endUpdate();
        }
      }
    }, 200);
  }

  pgsqlElement(name = 'pgsql') {
    setTimeout(() => {
      if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        try {
          this.graph.multiplicities.push(new mxMultiplicity(
            false, name, null, null, 0, 1, ['RO', 'LO'],
            'Postgres can have only one incoming node',
            null));

          this.graph.multiplicities.push(new mxMultiplicity(
            true, name, null, null, 0, 1, ['RO', 'LO'],
            'Postgres can have only one incoming node',
            null));

          const img = '/assets/images/pgsql.png';
          this.graph.insertVertex(parent, null, 'pgsql', 400, 100, 80, 80, `shape=image;image=${img};resizable=0`);

          const request = {
            projectId: this.projectId,
            userId: this.userId,
            engFlowId: this.flowId,
            eventType: 'db'
          };

          this.engineeringService.createPgsqlFileEvent(request)
            .pipe(takeUntil(this.unsubscribe))
            .subscribe();
        } finally {
          this.graph.getModel().endUpdate();
        }
      }
    }, 200);
  }

  deleteElement() {
    // Delete the selected elements using toolbar delete button or keyboard delete button.

    if (this.graph.isEnabled() && !(this.isPreview || this.viewOnly)) {
      if (this.graph.isSelectionEmpty()) {
        return;
      }

      const selectedCells = this.graph.getSelectionCells();
      this.isPreviewNew = false;

      if (selectedCells.length) {
        for (let cell of selectedCells) {
          if (this.graph.getModel().isEdge(cell)) {
            this.graph.removeCells([cell]);
          } else {

            this.engineeringService.deleteEvent(this.projectId, cell.id).subscribe((res) => {
              this.snackbarService.open(res.message);
              this.graph.removeCells([cell]);

              // Remove from graph model
              const cells = this.graph.getModel().cells;
              for (const key in cells) {
                if (cells[key]?.id === cell?.id) {
                  delete cells[key]
                }
              }

              const index = this.elements.findIndex(x => x === cell.id);
              this.elements.splice(index, 1);
              this.removeFromConfig(cell);
            }, (error) => {
              this.snackbarService.open(error);
            });
          }
        }
      }
    }
  }

  addJoinElement(name = 'inner') {
    this.joinsCount = this.joinsCount + 1;
    setTimeout(() => {
      if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();

        try {
          // const xmlDocument = mxUtils.createXmlDocument();
          // const sourceNode = xmlDocument.createElement(name);
          this.graph.multiplicities.push(
            new mxMultiplicity(false, name, null, null, 0, 2, ['circle'], 'Join can have only two sources', null));

          const yPosition = this.getYPostion(this.graph?.model?.cells, 'join');

          const img = 'image=/assets/images/join.svg';
          const vertex = this.graph.insertVertex(parent, null, name, 250, yPosition, 80, 80, `shape=image;${img};resizable=0`);
          const d = {
            projectId: this.projectId,
            type: 'join',
            engFlowId: this.flowId
          };

          this.engineeringService.createJoinEvent(d).subscribe((res) => {
            vertex.id = res.result;
            this.elements.push(res.result);
            this.configData.selectedJoins.push({
              engFlowId: this.flowId,
              projectId: this.projectId,
              joinName: name,
              joinType: name,
              eventId1: null,
              eventId2: null,
              dfId: null,
              firstFileColumn: '',
              secondFileColumn: '',
              eventId: res.result,
              autoConfigEventId: res.result,
              metaData: undefined,
              joinFolders: undefined
            });
          });
        } finally {
          this.graph.getModel().endUpdate();
        }
      }
    }, 200);
  }

  addUdfElement(d: any) {
    // const name = d.udfName;
    // const udf_syntax = d.udfSyntax;
    // const udf_argumnts = d.arguments;
    // const udf_id = d.udfId;

    this.udf_type_name = d.udfName;
    this.udf_syntax = d.udfSyntax;
    this.udf_arguments = d.arguments
    this.udf_id = d.udfId

    this.joinsCount = this.joinsCount + 1;
    setTimeout(() => {
      if (!mxClient.isBrowserSupported()) {
        mxUtils.error('Browser is not supported!', 200, false);
      } else {
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();

        try {
          // const xmlDocument = mxUtils.createXmlDocument();
          // const sourceNode = xmlDocument.createElement(name);
          this.graph.multiplicities.push(
            new mxMultiplicity(false, name, null, null, 0, 5, ['circle'], 'UDF will have maximum five sources', null));

          const yPosition = this.getYPostion(this.graph?.model?.cells, 'join');

          const img = 'image=/assets/images/udf-applied-icon-blue.svg';
          const vertex = this.graph.insertVertex(parent, null, name, 250, yPosition, 80, 80, `shape=image;${img};resizable=0`);
          const obj = {
            projectId: this.projectId,
            fileType: 'udf',
            engFlowId: this.flowId,
            fileId: this.udf_id,
            eventIds: {
              eventId: 0
            }
          };

          this.engineeringService.createUdfEvent(obj).subscribe((res) => {
            vertex.id = res.result;
            this.elements.push(res.result);
            this.configEventId = res.result;

            this.configData.selectedUdfs.push({
              engFlowId: this.flowId,
              projectId: this.projectId,
              joinName: this.udf_type_name,
              joinType: this.udf_type_name,
              eventId1: null,
              eventId2: null,
              dfId: null,
              firstFileColumn: '',
              secondFileColumn: '',
              eventId: null,
              eventIds: {
                eventId: res.result,
              },
              autoConfigEventId: res.result,
              metaData: undefined,
              joinFolders: undefined,
              udfFunction: this.udf_syntax,
              arguments: this.udf_arguments,
              fileType: 'udf',
              fileId: this.udf_id,
              variable: '',
            });
          });
        } finally {
          this.graph.getModel().endUpdate();
        }
      }
    }, 200);
  }

  addVertex(fileData: any) {

    // user cannot drag same file.
    const exists = this.configData.selectedFiles.find(t => t.fileId === fileData.fileId && t.fileType === fileData.category);
    if (exists) {
      this.snackbarService.open('Cannot drag the same file.');
      return;
    }

    if (!mxClient.isBrowserSupported()) {
      mxUtils.error('Browser is not supported!', 200, false);
    } else {
      const parent = this.graph.getDefaultParent();
      this.graph.getModel().beginUpdate();
      try {

        this.graph.multiplicities.push(
          new mxMultiplicity(false, fileData.fileName, null, null, 0, 0, null, 'Source can have only one Target', null));

        this.graph.multiplicities.push(
          new mxMultiplicity(true, fileData.fileName, null, null, 0, 1, null, 'Source can have only one Target', null));

        // const color = this.colorOptions[this.getRandomArbitrary()];

        const d = {
          projectId: this.projectId,
          folderId: fileData.folderId,
          fileId: fileData.fileId,
          engFlowId: this.flowId,
          engFlowName: fileData.fileName,
          fileType: fileData.category
        };

        const yPosition = this.getYPostion(this.graph?.model?.cells, 'file');

        const img = fileData.category === 'eng' ? EngFileImages.Inital : FileImages.Inital;

        const vertex = this.graph.insertVertex(parent, null,
          fileData.fileName, 10, yPosition, 80, 80, `shape=image;image=${img};resizable=0`);

        this.engineeringService.createFileEvent(d).subscribe((res) => {
          const metaData = JSON.parse(res.metadata);
          vertex.id = res.result;
          this.elements.push(res.result);
          const file = {
            engFlowId: this.flowId,
            projectId: this.projectId,
            eventId: res?.result,
            autoConfigEventId: res?.result,
            name: fileData?.fileName,
            folderId: fileData?.folderId,
            fileId: fileData?.fileId,
            type: 'file',
            metaData: metaData?.metadata,
            fileType: fileData?.category
          };

          this.configData.selectedFiles.push(file);


          this.intervalCall();
        });

      } finally {
        this.graph.getModel().endUpdate();
      }
    }
  }

  private getPreviewContent(): void {

    const previewParam: PreviewParam = {
      projectId: `${this.projectId}`,
      engFlowId: this.flowId,
      eventId: `${0}`,
      userId: this.userId
    };

    this.engineeringService.getPreviewContent(previewParam).subscribe((res: any) => {

      if (res?.result !== '{}') {
        const result = res?.result;

        if (!res.trigger_next) {
          this.subscription.unsubscribe();
        }

        this.updateCanvasImages(result);
      }
    }, () => {
      this.subscription.unsubscribe();
    });
  }

  private updateCanvasImages(result: any): void {

    result.map(t => {
      if (t.eventType === 'join') {
        const join = this.configData.selectedJoins.find(x => x.autoConfigEventId === t.autoConfigEventId);
        if (!join) {
          return;
        }

        if (!join.content) {
          join.content = new PreviewContent();
        }

        join.content.eventProgress = t.eventProgress > 20 ? t.eventProgress : 15;
        join.content.disabledPreview = t.eventProgress > 0;
        join.content.viewOnly = this.viewOnly;

        if (join && t?.engFlowEventData !== null) {
          join.content.eventProgress = 100;
          join.content.isDisabledApply = false;
          let data = JSON.parse(t?.engFlowEventData);
          data = data.filter(t => Object.keys(t).length !== 0);

          if (data.length) {
            join.content.rowData = data;
            join.content.rowColumns = Object.keys(data[0]);
            join.content.disabledPreview = true;
          }

          if (join?.joinType?.toLowerCase() === 'inner') {
            this.updateImageWithStatus({ eventId: join.eventId, image: InnerJoinImages.Success });
          } else if (join?.joinType?.toLowerCase() === 'left') {
            this.updateImageWithStatus({ eventId: join.eventId, image: LeftJoinImages.Success });
          } else if (join?.joinType?.toLowerCase() === 'right') {
            this.updateImageWithStatus({ eventId: join.eventId, image: RightJoinImages.Success });
          } else if (join?.joinType?.toLowerCase() === 'full') {
            this.updateImageWithStatus({ eventId: join.eventId, image: FullJoinImages.Success });
          }
        }
      } else if (t.eventType === 'file' || t.eventType === 'eng') {
        // assing join data to variable
        const file = this.configData.selectedFiles.find(x => x.autoConfigEventId === t.autoConfigEventId);

        if (!file) {
          return;
        }

        if (!file.content) {
          file.content = new PreviewContent();
        }

        file.content.eventProgress = t.eventProgress > 20 ? t.eventProgress : 15;

        if (t.engFlowEventData !== null) {
          file.content.eventProgress = 100;
          let data = JSON.parse(t?.engFlowEventData);
          data = data.filter(t => Object.keys(t).length !== 0);

          if (data.length) {
            file.content.rowData = data;
            file.content.rowColumns = Object.keys(data[0]);
          }

          this.updateImageWithStatus({
            eventId: file.eventId,
            image: file.fileType === 'eng' ? EngFileImages.Success : FileImages.Success
          });
        }
      } else if (t.eventType === 'agg') {
        const aggs = this.configData.selectedAggregates.find(x => x.autoConfigEventId === t.autoConfigEventId);
        if (!aggs) {
          return;
        }

        if (!aggs.content) {
          aggs.content = new PreviewContent();
        }
        aggs.content.eventProgress = t.eventProgress > 20 ? t.eventProgress : 15;
        aggs.content.disabledPreview = t.eventProgress > 0;
        aggs.content.viewOnly = this.viewOnly;

        if (aggs && t?.engFlowEventData !== null) {
          aggs.content.eventProgress = 100;
          aggs.content.isDisabledApply = false;
          const tmpData = t?.engFlowEventData.replace(/null/g, '"null"');
          let data = JSON.parse(tmpData);
          data = data.filter(t => Object.keys(t).length !== 0);

          if (data.length) {
            aggs.content.rowData = data;
            aggs.content.rowColumns = Object.keys(data[0]);
            aggs.content.disabledPreview = true;
          }

          this.updateImageWithStatus({ eventId: aggs.eventId, image: AggregateImages.Success });
        }
      } else if (t.eventType === 'udf') {
        const udf = this.configData.selectedUdfs.find(x => x.autoConfigEventId === t.autoConfigEventId);

        if (!udf) {
          return;
        }

        if (!udf.content) {
          udf.content = new PreviewContent();
        }

        udf.content.eventProgress = t.eventProgress > 20 ? t.eventProgress : 15;

        if (t.livyStmtExecutionStatus == "error") {
          udf.content.eventProgress = 100;
          let data = JSON.parse(t?.engFlowEventData);
          // data = data.filter(t => Object.keys(t).length !== 0);
          udf.content.rowData = data.evalue;

          // if (data.length) {
          //   udf.content.rowData = data;
          //   udf.content.rowColumns = Object.keys(data[0]);
          // }

          this.updateImageWithStatus({ eventId: udf.autoConfigEventId, image: UdfImages.Success });
        } else if (t.engFlowEventData !== null) {
          udf.content.eventProgress = 100;
          udf.content.disabledPreview = t.eventProgress > 0;
          let data = JSON.parse(t?.engFlowEventData);
          data = data.filter(t => Object.keys(t).length !== 0);
          udf.joinOperations = t.joinOperations;

          if (data.length) {
            udf.content.rowData = data;
            udf.content.rowColumns = Object.keys(data[0]);
          }

          this.updateImageWithStatus({ eventId: udf.autoConfigEventId, image: UdfImages.Success });
        }
      }
    });
  }

  getYPostion(cells: any, type = 'file'): number {
    let yPosition = 10;
    for (const index in cells) {
      const item = cells[index];
      if (item.value) {
        const selectedMenuType = this.getSelectedToolbarType(item.value);
        if (type == 'file' && item.geometry && !selectedMenuType) {
          yPosition = item?.geometry?.y == 0 ? 100 : item?.geometry?.y + 100;
        } else if (type == 'join' && item.geometry && this.joins.includes(item.value)) {
          yPosition = item?.geometry?.y == 0 ? 100 : item?.geometry?.y + 100;
        } else if (item.value.toLowerCase() == type && item.geometry) {
          yPosition = item?.geometry?.y == 0 ? 100 : item?.geometry?.y + 100;
        }
      }
    }

    return yPosition;
  }

  allowDrop(ev) {
    ev.preventDefault();
  }

  drop(ev) {
    ev.preventDefault();
    const fileStr = ev.dataTransfer.getData('text');
    const fileJson = JSON.parse(fileStr);

    setTimeout(() => {
      this.addVertex(fileJson);
    }, 200);
  }

  dropFile(fileStr: string): void {
    const fileJson = JSON.parse(fileStr);

    setTimeout(() => {
      this.addVertex(fileJson);
    }, 200);
  }

  private getSelectionCell(eventId: number): any {
    const cells = this.graph.getModel().cells;
    for (const key in cells) {
      const mxCell = cells[key];
      if (+mxCell.id === eventId) {
        return mxCell;
      }
    }
  }

  public getPreviewData(): void {
    this.intervalCall();
  }

  public saveJoinData(d): void {
    let cell = this.getSelectionCell(d.eventId);

    const evtId1 = +cell.edges[0].source.id;
    const evtId2 = +cell.edges[1].source.id;
    const joinType = d.joinType;

    const request = {
      eventId: +cell.id,
      eventId1: evtId1,
      eventId2: evtId2,
      dfId: null,
      projectId: +this.projectId,
      engFlowId: this.flowId,
      joinType,
      firstFileColumn: d?.firstFileColumn,
      secondFileColumn: d?.secondFileColumn
    };

    this.engineeringService.createJoinEvent(request).subscribe((res) => {
      const selectedJoin = this.configData.selectedJoins.find(j => j.eventId === +cell.id);
      const idx = this.configData.selectedJoins.findIndex(j => j.eventId === +cell.id);

      cell.setValue(d.joinType);
      this.updateJoin(cell);

      const allEvents = [...this.configData.selectedFiles, ...this.configData.selectedJoins, ...this.configData.selectedUdfs];
      const columns1 = allEvents.find(f => f.eventId === evtId1 || f.autoConfigEventId === evtId1).metaData ?? [];
      const columns2 = allEvents.find(f => f.eventId === evtId2 || f.autoConfigEventId === evtId2).metaData ?? [];
      const tempColumns = [...columns1, ...columns2];

      const columns = [];
      // const map = new Map();
      for (const item of tempColumns) {
        columns.push({
          key: item.key,
          value: item.value
        });
        // if (!map.has(item.value)) {
        //   map.set(item.value, true);
        //   columns.push({
        //     key: item.key,
        //     value: item.value
        //   });
        // }
      }

      selectedJoin.joinName = `Join_${idx}`;
      selectedJoin.joinType = joinType;
      selectedJoin.eventId1 = evtId1;
      selectedJoin.eventId2 = evtId2;
      selectedJoin.metaData = columns;
      selectedJoin.firstFileColumn = d?.firstFileColumn;
      selectedJoin.secondFileColumn = d?.secondFileColumn;

      this.selectedJoin.firstFileColumn = selectedJoin.firstFileColumn;
      this.selectedJoin.secondFileColumn = selectedJoin.secondFileColumn;

      this.snackbarService.open('Join applied successfully');
      this.intervalCall();

    }, (error) => {
      this.snackbarService.open(`Error - ${error}`);
    });
  }

  public saveUdfData(d): void {
    let cell = this.getSelectionCell(d.eventIds.eventId);;

    if (!cell) {
      cell = this.graph.getSelectionCell();
    }

    this.eventIdData = { eventId: +cell.id };

    this.event_id_1 = +cell?.edges[0]?.source?.id;
    this.event_id_2 = +cell?.edges[1]?.source?.id;
    this.event_id_3 = +cell?.edges[2]?.source?.id;
    this.event_id_4 = +cell?.edges[3]?.source?.id;
    this.event_id_5 = +cell?.edges[4]?.source?.id;

    switch (cell.edges.length) {
      case 1:
        this.eventIdData = { eventId: +cell.id, eventId1: this.event_id_1 };
        this.df = this.prePareUdfFunction(d.udfFunction, 1, d.variable);
        break;
      case 2:
        this.eventIdData = { eventId: +cell.id, eventId1: this.event_id_1, eventId2: this.event_id_2 };
        this.df = this.prePareUdfFunction(d.udfFunction, 2, d.variable);
        break;
      case 3:
        this.eventIdData = { eventId: +cell.id, eventId1: this.event_id_1, eventId2: this.event_id_2, eventId3: this.event_id_3, };
        this.df = this.prePareUdfFunction(d.udfFunction, 3, d.variable);
        break;
      case 4:
        this.eventIdData = { eventId: +cell.id, eventId1: this.event_id_1, eventId2: this.event_id_2, eventId3: this.event_id_3, eventId4: this.event_id_4, };
        this.df = this.prePareUdfFunction(d.udfFunction, 4, d.variable);
        break;
      case 5:
        this.eventIdData = { eventId: +cell.id, eventId1: this.event_id_1, eventId2: this.event_id_2, eventId3: this.event_id_3, eventId4: this.event_id_4, eventId5: this.event_id_5 };
        this.df = this.prePareUdfFunction(d.udfFunction, 5, d.variable);
        break;
    }

    const joinType = d.file_type;

    const request = {
      eventIds: this.eventIdData,
      dfId: null,
      projectId: +this.projectId,
      engFlowId: this.flowId,
      fileId: d.udfId,
      fileType: 'udf',
      udfFunction: d.udfFunction,
      arguments: d.arguments,
    };

    // Find index of specific object using findIndex method.    
    const objIndex = this.configData.selectedUdfs.findIndex((obj => obj.fileType == "udf"));

    // Update object's name property.
    this.configData.selectedUdfs[objIndex].eventIds = this.eventIdData;

    this.engineeringService.createUdfEvent(request).subscribe((res) => {
      const selectedUdf = this.configData.selectedUdfs.find(j => j.autoConfigEventId === +cell.id);
      const idx = this.configData.selectedUdfs.findIndex(j => j.autoConfigEventId === +cell.id);

      cell.setValue(d.file_type);

      const allEvents = [...this.configData.selectedFiles, ...this.configData.selectedUdfs];
      const columns1 = allEvents.find(f => f.eventId === this.event_id_1)?.metaData ?? [];
      const columns2 = allEvents.find(f => f.eventId === this.event_id_2)?.metaData ?? [];
      const tempColumns = [...columns1, ...columns2];

      const columns = [];
      for (const item of tempColumns) {
        columns.push({ key: item.key, value: item.value });
      }

      selectedUdf.joinName = `Udf_${idx}`;
      selectedUdf.joinType = joinType;
      selectedUdf.metaData = columns;
      selectedUdf.firstFileColumn = d?.firstFileColumn;
      selectedUdf.secondFileColumn = d?.secondFileColumn;

      this.selectedUdf.firstFileColumn = selectedUdf.firstFileColumn;
      this.selectedUdf.secondFileColumn = selectedUdf.secondFileColumn;

      this.snackbarService.open(res.message);
      this.intervalCall();

    }, (error) => {
      this.snackbarService.open(`Error - ${error}`);
    });
  }

  private prePareUdfFunction(udfSyntax: string, noOfUdfEvent: number, variables: any[]): string {
    let prepare_function = '';

    // push selected event data into array.
    const udfEventData = [];
    variables.map(t => { udfEventData.push(t.variable) });
    for (let index = 1; index <= noOfUdfEvent; index++) {
      udfEventData.push(eval(`this.event_id_${index}`));
    }

    for (const item of udfEventData) {
      if (Number.isInteger(item)) {
        prepare_function += `df${item},`;
      } else {
        prepare_function += `'${item}',`;
      }
    }

    // get function name
    const functionName = udfSyntax.substring(0, udfSyntax.indexOf('('));

    // remove last comma from prepared function string.
    prepare_function = prepare_function.slice(0, -1);
    prepare_function = `${functionName}(${prepare_function})`;

    return prepare_function;
  }

  updateJoinImage(data: any): void {
    let cell = this.graph.getSelectionCell();

    if (!cell) {
      cell = this.getSelectionCell(data.eventId);
    }

    cell.setValue(data.joinType);
    this.updateJoin(cell);
  }

  updateJoin(cell: any) {
    this.graph.getModel().beginUpdate();
    try {
      cell.style = `shape=image;image=${this.getSelectedJoinImage(cell.value)};resizable=0`;
    }
    finally {
      this.graph.getModel().endUpdate();
      this.graph.refresh();
    }
  }

  getSelectedJoinImage(joinType: string): string {
    switch (joinType) {
      case 'right':
        return RightJoinImages.Inital;
      case 'left':
        return LeftJoinImages.Inital;
      case 'full':
        return FullJoinImages.Inital;
      default:
        return InnerJoinImages.Inital;
    }
  }

  updateUdf(cell: any) {
    this.graph.getModel().beginUpdate();
    try {
      cell.style = `shape=image;image=${this.getSelectedUdfImage(cell.value)};resizable=0`;
    }
    finally {
      this.graph.getModel().endUpdate();
      this.graph.refresh();
    }
  }

  getSelectedUdfImage(joinType: string): string {
    return UdfImages.Success;
  }

  getSelectedToolbarType(name: any): string {
    let value = '';
    switch (name) {
      case 'udf':
        value = 'udf';
        break;
      case 'pgsql':
        value = 'pgsql';
        break;
      case 'right':
      case 'left':
      case 'full':
      case 'inner':
        value = name;
        break;
    }

    return value;
  }

  removeFromConfig(cell): void {
    // join
    if (this.isJoin(cell)) {
      const index = this.configData.selectedJoins.findIndex(x => x.eventId === cell.id);
      this.configData.selectedJoins.splice(index, 1);
    }

    // file
    const idx = this.configData.selectedFiles.findIndex(x => x.eventId === +cell.id);
    if (idx >= 0) {
      this.configData.selectedFiles.splice(idx, 1);

      // also delete from associated join data
      this.configData.selectedJoins.map(t => {
        const event1 = t.eventId1 === +cell.id;
        const event2 = t.eventId2 === +cell.id;

        if (event1) {
          t.eventId1 = t.firstFileColumn = null;
        } else if (event2) {
          t.eventId2 = t.secondFileColumn = null;
        }
      });

      // aggregate
      const aggs = this.configData.selectedAggregates.find(x => x.dataFrameEventId === +cell.id);
      if (aggs) {
        aggs.columnsMetaData = [];
        aggs.groupByColumnsMetaData = [];
        aggs.groupByColumns = [];
      }
    }
    const aggIndex = this.configData.selectedAggregates.findIndex(x => x.eventId === +cell.id);
    if (aggIndex >= 0) {
      const aggs = this.configData.selectedAggregates[aggIndex];
      this.configData.selectedAggregates.splice(aggIndex, 1);

      const file = this.configData.selectedFiles.find(x => x.eventId === aggs.dataFrameEventId);

      if (file && file?.content?.rowColumns?.count > 0) {
        const metaData = [];
        // rebuild the metadata columne, with existing order.
        for (const key in file.content.rowColumns) {
          const col = file.content.rowColumns[key];
          metaData.push({ key: `a_${file.eventId}_${col}`, value: col });
        }
        file.metaData = metaData;
      } else {
        // get the data from aggregate object.

        const metaData = [];
        if (aggs.columnsMetaData.length) {
          aggs.columnsMetaData?.map(x => {
            metaData.push(x);
          });
        }
        if (aggs.groupByColumnsMetaData.length) {
          aggs.groupByColumnsMetaData?.map(x => {
            metaData.push(x);
          });
        }

        if (metaData.length) {
          metaData?.map(x => {
            file.metaData.push(x);
          });
        }
      }

      const join = this.configData.selectedJoins.find(x => x.eventId === aggs.dataFrameEventId);

      if (join && join?.content?.rowColumns?.count > 0) {
        const metaData = [];
        // rebuild the metadata column, with existing order.
        for (const key in join.content.rowColumns) {
          const value = join.content.rowColumns[key]?.replace(/a_[0-9]*_/, '');
          metaData.push({ key: join.content.rowColumns[key], value: value });
        }
        join.metaData = metaData;
      }
    }
  }

  isJoin(cell): boolean {
    return this.joins.some(t => t === cell?.value);
  }

  back(): void {
    this.router.navigate([`projects/${this.projectId}/engineering`]);
  }

  folderChange(folderName: string): void {
    this.folderName = folderName;
  }

  onSaveGraph(): void {
    this.saveGraph();
  }

  public onClosePreviewPanel(close: boolean): void {
    this.isPreviewNew = false;
  }

  public updateImageWithStatus(d: any): void {
    const cell = this.getSelectionCell(d.eventId);
    if (!cell) {
      return;
    }

    this.graph.getModel().beginUpdate();

    try {
      cell.style = `shape=image;image=${d.image};resizable=0`;
    }
    finally {
      this.graph.getModel().endUpdate();
      this.graph.refresh();
    }
  }

  ngOnDestroy(): void {
    // delete livy session
    // cmnted code for temporary
    // this.engineeringService.destroySession(this.userId).subscribe();

    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.quantumFacade.resetEngineeringData();
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}