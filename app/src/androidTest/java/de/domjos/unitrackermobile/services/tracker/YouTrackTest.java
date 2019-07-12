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

package de.domjos.unitrackermobile.services.tracker;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.tracker.YouTrack;
import de.domjos.unitrackermobile.utils.Helper;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
public class YouTrackTest {
    private IBugService<String> youtrack;

    @Before
    public void init() throws Exception {
        this.youtrack = new YouTrack(Helper.getAuthFromRes(R.raw.test_credentials, "youtrack"));
        for (Project project : this.youtrack.getProjects()) {
            this.youtrack.deleteProject(String.valueOf(project.getId()));
        }
    }

    @Test
    public void testProjects() throws Exception {
        List<Project<String>> projects = this.youtrack.getProjects();
        assertNotNull(projects);

        int count = projects.size();

        Project<String> project = new Project<>();
        project.setAlias("test");
        project.setTitle("Test");
        project.setDescription("This is a test!");
        String id = this.youtrack.insertOrUpdateProject(project);
        assertNotEquals("0", id);

        project.setId(id);
        project.setDescription("This is a new test!");
        id = this.youtrack.insertOrUpdateProject(project);

        projects = this.youtrack.getProjects();
        assertNotNull(projects);
        assertNotEquals(count, projects.size());
        for (Project current : projects) {
            if (id.equals(String.valueOf(current.getId()))) {
                Project selected = this.youtrack.getProject(String.valueOf(current.getId()));
                assertNotNull(selected);
                assertEquals("This is a new test!", selected.getDescription());
                break;
            }
        }

        this.youtrack.deleteProject(id);
        assertEquals(count, this.youtrack.getProjects().size());
    }
}
