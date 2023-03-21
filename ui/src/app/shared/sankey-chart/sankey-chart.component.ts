import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import * as d3Format from 'd3-format';
import * as d3Sankey from 'd3-sankey';
import * as d3Scale from 'd3-scale';
import * as d3ScaleChromatic from 'd3-scale-chromatic';
import * as d3Selection from 'd3-selection';
import * as zoom from 'd3-zoom';

import { jsPDF } from 'jspdf';
import 'svg2pdf.js';


export enum Alignment {
  Center = 'Center',
  Left = 'Left',
  Right = 'Right',
  Justify = 'Justify'
}

@Component({
  selector: 'app-sankey-chart',
  templateUrl: './sankey-chart.component.html',
  styleUrls: ['./sankey-chart.component.scss']
})
export class SankeyChartComponent implements OnInit {
  @Input() nodes: any;
  @Input() links: any;
  @Input() legends: any;

  readonly nodess = [
    {
      name: 'Folder1',
      category: 'Folder',
      testProp: 'ABC',
      size: 1
    },
    {
      name: 'Folder2',
      category: 'Folder',
      testProp: 'Test',
      size: 1
    },
    {
      name: 'File1',
      category: 'File',
      testProp: 'Test',
      size: 1
    },
    {
      name: 'File2',
      category: 'File',
      testProp: 'Test',
      size: 1
    },
    {
      name: 'Join1',
      category: 'Join',
      testProp: 'Test',
      size: 1
    },
    {
      name: 'agg1',
      category: 'Aggregate',
      testProp: 'Test',
      size: 1
    }
  ];

  readonly linkss = [
    {
      source: 'Folder1',
      target: 'File1',
      value: 10
    },
    {
      source: 'Folder2',
      target: 'File2',
      value: 10
    },
    {
      source: 'File1',
      target: 'Join1',
      value: 10
    },
    {
      source: 'File2',
      target: 'Join1',
      value: 10
    },
    {
      source: 'Join1',
      target: 'agg1',
      value: 10
    }
  ];

  @ViewChild('chart') private chartContainer: ElementRef;
  @ViewChild('tooltip') private tooltip: ElementRef;
  @Input() alignment = Alignment.Right;
  @Input() nodeWidth = 15;
  @Input() nodePadding = 10;

  public showPanel = false;
  public nodeInfo: any;
  public svg: any;
  private margin: any = { top: 20, bottom: 20, left: 20, right: 20 };
  private chart: any;
  private width: number;
  private height: number;
  private colorScale = d3Scale.scaleOrdinal(d3ScaleChromatic.schemeCategory10);
  constructor() { }

  ngOnInit() {
    setTimeout(() => {
      this.createChart();
      // if (this.nodes && this.links) {
      this.updateChart();
      // }
    }, 100);

  }

  generateData2(data) {
    const { nodes, links } = d3Sankey.sankey(data);

    return {
      nodes,
      links
    };

  }

  generateData() {
    const element = this.chartContainer.nativeElement;
    const width = element.offsetWidth;
    const height = element.offsetHeight;

    const alignMethod = d3Sankey[`sankey${this.alignment}`];
    const sankeyGeneratorFn = d3Sankey.sankey()
      .nodeId(d => d.name)
      .nodeAlign(alignMethod)
      .nodeWidth(this.nodeWidth)
      .nodePadding(this.nodePadding)
      .extent([
        [20, 20],
        [width - this.margin.left - this.margin.right, height - this.margin.top - this.margin.bottom]]);

    return ({ nodes, links }) => sankeyGeneratorFn({
      nodes: nodes.map(d => Object.assign({}, d)),
      links: links.map(d => Object.assign({}, d))
    });
  }

  createChart() {
    const element = this.chartContainer.nativeElement;
    this.width = element.offsetWidth - this.margin.left - this.margin.right;
    this.height = element.offsetHeight - this.margin.top - this.margin.bottom;

    const z = zoom.zoom()
      .scaleExtent([1, 10])
      // .translateExtent([[0, 0], [this.width, this.height]])
      .on('zoom', this.zoomed.bind(this));

    this.svg = d3Selection.select(element).append('svg')
      .attr('width', element.offsetWidth)
      .attr('height', element.offsetHeight)
      .call(z)
      .on('dblclick.zoom', null);

    d3Selection.select('#zoom_in').on('click', () => {
      z.scaleBy(this.svg.transition().duration(750), 1.2);
    });

    d3Selection.select('#zoom_out').on('click', () => {
      z.scaleBy(this.svg.transition().duration(750), 0.8);
    });

    // chart plot area
    this.chart = this.svg.append('g')
      .attr('class', 'sankey')
      .attr('transform', `translate(${this.margin.left}, ${this.margin.top})`);
  }

  private zoomed(e: any): void {
    this.svg.attr('transform', e.transform);
  }

  updateChart() {
    const { nodes, links } = this.generateData()({ nodes: this.nodes, links: this.links });
    this.drawNodes(nodes);
    this.drawLinks(links);
    this.drawLinkLabels(nodes, links);
  }

  drawNodes(nodes: Array<any>) {
    this.chart.append('g')
      .attr('stroke', '#000')
      .selectAll('rect')
      .data(nodes)
      .join('rect')
      .on('click', (e, d) => {
        this.nodeInfo = { name: d.name, category: d.category, metadata: d.metadata };
        this.showPanel = true;
      })
      .attr('x', d => d.x0)
      .attr('y', d => d.y0)
      .attr('height', (d) => {
        return (d.y1 - d.y0);
      })
      .attr('width', d => d.x1 - d.x0)
      .attr('fill', (d, i) => this.color(d.name, d.category, i))
      .append('title')
      .text(d => `${d.name}\n${this.format(d.value)}`);

  }

  drawLinks(links: Array<any>) {
    const link = this.chart.append('g')
      .attr('class', 'links')
      .attr('fill', 'none')
      .attr('stroke-opacity', 0.5)
      .selectAll('g')
      .data(links)
      .join('g')
      .attr('class', 'link')
      .style('mix-blend-mode', 'multiply');

    const edgeColor = 'none';
    link
      .append('path')
      .attr('d', d3Sankey.sankeyLinkHorizontal())
      .attr('stroke', (d, i) => {
        return edgeColor === 'none' ? '#aaa'
          : edgeColor === 'path' ? d.uid
            : edgeColor === 'input' ? this.color(d.source.name, d.source.id)
              : this.color(d.target.name, d.source.id);
      })
      .attr('stroke-width', d => Math.max(1, d.width));

    // link.append("title")
    //     .text(d => `${d.source.name} → ${d.target.name}\n${d3Format.format(d.value)}`);

  }

  drawLinkLabels(nodes, links) {

    this.chart.selectAll('g.link')
      .append('title')
      .text(d => `${d.source.name} → ${d.target.name}\n${this.format(d.value)}`);

    this.chart.append('g')
      .style('font', '10px sans-serif')
      .selectAll('text')
      .data(nodes)
      .join('text')
      .attr('x', d => d.x0 < this.width / 2 ? d.x1 + 6 : d.x0 - 6)
      .attr('y', d => (d.y1 + d.y0) / 2)
      .attr('dy', '0.35em')
      .attr('text-anchor', d => d.x0 < this.width / 2 ? 'start' : 'end')
      .text(d => d.name);
  }

  get color() {
    return (name, category, i?) => {
      if (category === 'Folder') {
        return 'green';
      } else if (category === 'file') {
        return 'yellowgreen';
      } else if (category === 'join') {
        return 'red';
      } else if (category === 'agg') {
        return 'black';
      } else {
        return 'black';
      }
    };
  }

  get format() {
    const f = d3Format.format(',.0f');
    return d => `${f(d)}`;
  }

  public closePanel(): void {
    this.showPanel = false;
  }

  public download(): void {
    //

    // const doc = new jspdf();

    // const specialElementHandlers = {
    //   '#editor': function (element, renderer) {
    //     return true;
    //   }
    // };



    // const inner = document.querySelector('.page-header').innerHTML;
    // // this.chartContainer.nativeElement.getInnerHTML()
    // doc.fromHTML(inner, 15, 15, {
    //   'width': 190,

    // });
    // doc.save('sample-page.pdf');

    //// Working
    const doc = new jsPDF('landscape', 'pt', 'a2');

    const element = document.getElementsByTagName('svg')[0];
    doc
      .svg(element, { x: 10, y: 10 })
      .then(() => {
        doc.save('myPDF.pdf');
      });

    //// Working  end
  }

}


