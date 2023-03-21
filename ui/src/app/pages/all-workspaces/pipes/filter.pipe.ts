import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {

  transform(items: any[], searchText: string): unknown {
    if (!items || !searchText) return items;

    searchText = searchText.toLowerCase();

    return items.filter(it => {
      return it.projectDisplayName.toLowerCase().includes(searchText);
    });
  }
}
