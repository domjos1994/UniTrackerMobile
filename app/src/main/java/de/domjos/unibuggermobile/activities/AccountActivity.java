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

package de.domjos.unibuggermobile.activities;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.custom.AbstractActivity;

public final class AccountActivity extends AbstractActivity {

    public AccountActivity() {
        super(R.layout.account_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void initControls() {
        OnNavigationItemSelectedListener listener = menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAccountAdd:

                    break;
                case R.id.navAccountEdit:

                    break;
                case R.id.navAccountDelete:

                    break;
                case R.id.navAccountCancel:

                    break;
                case R.id.navAccountSave:

                    break;
            }
            return false;
        };
        BottomNavigationView navView = this.findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(listener);
    }


}
