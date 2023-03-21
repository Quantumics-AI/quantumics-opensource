import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GraphComponent } from './graph.component';
import { ChartsModule } from 'ng2-charts';
import { LoaderModule } from '../loader/loader.module';

@NgModule({
    declarations: [GraphComponent],
    imports: [
        CommonModule,
        ChartsModule,
        LoaderModule
    ],
    exports: [GraphComponent]
})
export class GraphModule { }
