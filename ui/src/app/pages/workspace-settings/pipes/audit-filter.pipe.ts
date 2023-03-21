import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'auditFilter'
})
export class AuditFilterPipe implements PipeTransform {

  transform(values: Array<any>, eventTypes: Array<string>, users: Array<string>, update: boolean = false): Array<any> {
    if (!values) {
      return;
    }

    if (eventTypes.length) {
      values = values.filter(a => eventTypes.some(e => e === a.eventType.toLowerCase()));
    }

    if (users.length) {
      values = values.filter(a => users.some(e => e.trim() === a.userName.trim()));
    }

    return values;
  }

}
