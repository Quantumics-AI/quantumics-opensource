import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CleansingRoutingModule } from './cleansing-routing.module';
import { CleansingComponent } from './cleansing.component';
import { CleansingToolbarComponent } from './components/cleansing-toolbar/cleansing-toolbar.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { ChartsModule } from 'ng2-charts';
import { ReplaceTextOrPatternComponent } from './components/replace-text-or-pattern/replace-text-or-pattern.component';
import { ReplaceCellComponent } from './components/replace-cell/replace-cell.component';
import { ReplaceMissingComponent } from './components/replace-missing/replace-missing.component';
import { FormatComponent } from './components/format/format.component';
import { MergeComponent } from './components/merge/merge.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgbModule, NgbTooltipModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { RulesCatelogueComponent } from './components/rules-catelogue/rules-catelogue.component';
import { SplitComponent } from './components/split/split.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { SortByPipe } from '../cleansing/pipes/sort-by.pipe';
import { CountMatchesComponent } from './components/count-matches/count-matches.component';
import { AddCleansingComponent } from './components/add-cleansing/add-cleansing.component';
import { ListRawDataComponent } from './components/list-raw-data/list-raw-data.component';
import { ListCleansedFilesComponent } from './components/list-cleansed-files/list-cleansed-files.component';
import { LoaderModule } from '../../core/components/loader/loader.module';
import { GraphModule } from '../../core/components/graph/graph.module';
import { RulesEditorContainerComponent } from './components/rules-editor-container/rules-editor-container.component';
import { RemoveRowsComponent } from './components/remove-rows/remove-rows.component';
import { RemoveRowsByColumnValuesComponent } from './components/remove-rows-by-column-values/remove-rows-by-column-values.component';
import { ManageColumnsComponent } from './components/manage-columns/manage-columns.component';
import { RemoveDuplicatesComponent } from './components/remove-duplicates/remove-duplicates.component';
import { ExtractColumnValuesComponent } from './components/extract-column-values/extract-column-values.component';
import { StandardizeComponent } from './components/standardize/standardize.component';
import { DeleteComfirmationComponent } from './components/delete-comfirmation/delete-comfirmation.component';
import { QsMaxLengthDirective, PositiveNumberOnlyDirective } from './directives/qs-max-length.directive';
import { FilterPipeModule } from 'ngx-filter-pipe';

@NgModule({
  declarations: [
    CleansingComponent,
    CleansingToolbarComponent,
    ReplaceTextOrPatternComponent,
    ReplaceCellComponent,
    ReplaceMissingComponent,
    FormatComponent,
    MergeComponent,
    RulesCatelogueComponent,
    SplitComponent,
    SortByPipe,
    CountMatchesComponent,
    AddCleansingComponent,
    ListRawDataComponent,
    ListCleansedFilesComponent,
    RulesEditorContainerComponent,
    RemoveRowsComponent,
    RemoveRowsByColumnValuesComponent,
    ManageColumnsComponent,
    RemoveDuplicatesComponent,
    ExtractColumnValuesComponent,
    StandardizeComponent,
    DeleteComfirmationComponent,
    QsMaxLengthDirective,
    PositiveNumberOnlyDirective
  ],
  imports: [
    CommonModule,
    CleansingRoutingModule,
    NgxSpinnerModule,
    ChartsModule,
    NgbModule,
    NgbTooltipModule,
    NgbDropdownModule,
    DragDropModule,
    ReactiveFormsModule, FormsModule,
    NgSelectModule,
    LoaderModule,
    GraphModule,
    FilterPipeModule
  ]
})

export class CleansingModule { }
