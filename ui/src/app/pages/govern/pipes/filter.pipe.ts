import { Pipe, PipeTransform } from '@angular/core';
import * as lodash from 'lodash';

@Pipe({ name: 'filterBy' })
export class FilterPipe implements PipeTransform {
    transform(value, keys: string, term: string): any[] {
        if (!term) {
            return value;
        }

        return (value || [])
            .filter(item =>
                keys.split(',').some(key => {
                    const val = lodash.get(item, key.trim(), undefined);
                    return val !== undefined && new RegExp(term, 'gi').test(val);
                })
            );
    }
}
