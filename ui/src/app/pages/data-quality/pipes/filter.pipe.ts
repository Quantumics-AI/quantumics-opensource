import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {

  transform(items: any[], filterBy: string, type: 'all' | 'db' | 'crm'): any {

    if (type && type !== 'all') {
      items = items.filter(i => i.dataSourceType.toLowerCase() === type);
    }

    if (filterBy) {
      items = items.filter(item => item.dataSourceName.toLowerCase()?.indexOf(filterBy.toLocaleLowerCase()) !== -1);
    }


    return items;
  }

}

@Pipe({
  name: 'filterDatabase',
  pure: false
})
export class FilterDatabasePipe implements PipeTransform {
  transform(items: any[], filter: Object): any {
      if (!items || !filter) {
          return items;
      }

      return items.filter(item => item.name.indexOf(filter) !== -1);
  }
}
