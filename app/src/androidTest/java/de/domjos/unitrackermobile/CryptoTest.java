/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.domjos.unibuggerlibrary.utils.Crypto;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
public class CryptoTest {

    @Test
    public void testEncryption() throws Exception {
        Crypto crypto = new Crypto(Helper.getContext(), "dfasdfsdafasdf");
        String encrypted = crypto.encryptString("dies ist ein test");
        String decrypted = crypto.decryptString(encrypted);
        assertEquals(decrypted, "dies ist ein test");
    }
}
