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

package de.domjos.unibugger.services.tracker;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.domjos.unibugger.interfaces.IBugService;
import de.domjos.unibugger.model.projects.Project;
import de.domjos.unibugger.utils.Helper;
import de.domjos.unibuggermobile.R;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class MantisBTTest {
    private IBugService mantisBT;

    @Before
    public void init() throws Exception {
        this.mantisBT = new MantisBT(Helper.getAuthFromRes(R.raw.test_credentials, "mantisbt"));
        for(Project project : this.mantisBT.getProjects()) {
            this.mantisBT.deleteProject(String.valueOf(project.getId()));
        }
    }

    @Test
    public void testProjects() throws Exception {
        List<Project> projects = this.mantisBT.getProjects();
        assertNotNull(projects);

        int count = projects.size();

        Project project = new Project();
        project.setAlias("test");
        project.setTitle("Test");
        project.setDescription("This is a test!");
        project.setEnabled(true);
        String id = this.mantisBT.insertOrUpdateProject(project);
        assertNotEquals(0, id);

        project.setId(Integer.parseInt(id));
        project.setDescription("This is a new test!");
        id = this.mantisBT.insertOrUpdateProject(project);

        projects = this.mantisBT.getProjects();
        assertNotNull(projects);
        assertNotEquals(count, projects.size());
        for(Project current : projects) {
            if(Integer.parseInt(id)==current.getId()) {
                Project selected = this.mantisBT.getProject(id);
                assertNotNull(selected);
                assertEquals("This is a new test!", selected.getDescription());
                break;
            }
        }

        this.mantisBT.deleteProject(String.valueOf(id));
        assertEquals(count, this.mantisBT.getProjects().size());
    }
}
