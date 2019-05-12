/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibugger.model.local;

import de.domjos.unibugger.model.objects.BaseObject;

public class LocalObject extends BaseObject {
    private boolean synced;
    private long last_synced;

    public LocalObject() {
        super();
        this.synced = false;
        this.last_synced = 0L;
    }

    public boolean isSynced() {
        return this.synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public long getLast_synced() {
        return this.last_synced;
    }

    public void setLast_synced(long last_synced) {
        this.last_synced = last_synced;
    }
}
