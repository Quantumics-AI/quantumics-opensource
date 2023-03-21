import { State } from '@ngrx/store';

export interface History {
    past: Array<any>;
    present: State<any>;
    future: Array<any>;
}
