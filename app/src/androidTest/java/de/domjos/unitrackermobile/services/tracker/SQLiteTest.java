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

package de.domjos.unitrackermobile.services.tracker;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.tracker.SQLite;
import de.domjos.unitrackermobile.utils.Helper;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
public class SQLiteTest {
    private IBugService<Long> sqLite;

    @Before
    public void init() throws Exception {
        Authentication authentication = new Authentication();
        authentication.setTracker(Authentication.Tracker.Local);
        authentication.setTitle("local");
        this.sqLite = new SQLite(Helper.getContext(), Helper.getVersionCode(Helper.getContext()), authentication);
        for (Project<Long> project : this.sqLite.getProjects()) {
            this.sqLite.deleteProject(project.getId());
        }
    }

    @Test
    public void testProjects() throws Exception {
        List<Project<Long>> projects = this.sqLite.getProjects();
        assertNotNull(projects);

        int count = projects.size();

        Project<Long> project = new Project<>();
        project.setAlias("test");
        project.setTitle("Test");
        project.setDescription("This is a test!");
        long id = this.sqLite.insertOrUpdateProject(project);
        assertNotEquals("0", id);

        project.setId(id);
        project.setDescription("This is a new test!");
        id = this.sqLite.insertOrUpdateProject(project);

        projects = this.sqLite.getProjects();
        assertNotNull(projects);
        assertNotEquals(count, projects.size());
        for (Project<Long> current : projects) {
            if (id == current.getId()) {
                Project selected = this.sqLite.getProject(current.getId());
                assertNotNull(selected);
                assertEquals("This is a new test!", selected.getDescription());
                break;
            }
        }

        this.sqLite.deleteProject(id);
        assertEquals(count, this.sqLite.getProjects().size());
    }
}
