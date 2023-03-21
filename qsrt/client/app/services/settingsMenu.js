import { isFunction, extend, omit, sortBy, find } from "lodash";
import { currentUser } from "@/services/auth";

class SettingsMenuItem {
  constructor(menuItem) {
    extend(this, { pathPrefix: `/${menuItem.path}` }, omit(menuItem, ["isActive"]));
    if (isFunction(menuItem.isActive)) {
      this.isActive = menuItem.isActive;
    }
  }

  isActive(path) {
    return path.startsWith(this.pathPrefix);
  }

  isAvailable() {
    return this.permission === undefined || currentUser.hasPermission(this.permission);
  }
}

class SettingsMenu {
  constructor() {
    this.items = [];
  }

  add(item) {
    this.items.push(new SettingsMenuItem(item));
    this.items = sortBy(this.items, "order");
  }

  getActiveItem(path) {
    return find(this.items, item => item.isActive(path));
  }
}

export default new SettingsMenu();
