package de.domjos.unitrackerlibrary.permissions;

import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;

public final class AzureDevOpsPermissions implements IFunctionImplemented {
    @Override
    public boolean listProjects() {
        return true;
    }

    @Override
    public boolean addProjects() {
        return true;
    }

    @Override
    public boolean updateProjects() {
        return true;
    }

    @Override
    public boolean deleteProjects() {
        return true;
    }

    @Override
    public boolean listVersions() {
        return false;
    }

    @Override
    public boolean addVersions() {
        return false;
    }

    @Override
    public boolean updateVersions() {
        return false;
    }

    @Override
    public boolean deleteVersions() {
        return false;
    }

    @Override
    public boolean listIssues() {
        return false;
    }

    @Override
    public boolean addIssues() {
        return false;
    }

    @Override
    public boolean updateIssues() {
        return false;
    }

    @Override
    public boolean deleteIssues() {
        return false;
    }

    @Override
    public boolean listNotes() {
        return false;
    }

    @Override
    public boolean addNotes() {
        return false;
    }

    @Override
    public boolean updateNotes() {
        return false;
    }

    @Override
    public boolean deleteNotes() {
        return false;
    }

    @Override
    public boolean listAttachments() {
        return false;
    }

    @Override
    public boolean addAttachments() {
        return false;
    }

    @Override
    public boolean updateAttachments() {
        return false;
    }

    @Override
    public boolean deleteAttachments() {
        return false;
    }

    @Override
    public boolean listRelations() {
        return false;
    }

    @Override
    public boolean addRelation() {
        return false;
    }

    @Override
    public boolean updateRelation() {
        return false;
    }

    @Override
    public boolean deleteRelation() {
        return false;
    }

    @Override
    public boolean listUsers() {
        return false;
    }

    @Override
    public boolean addUsers() {
        return false;
    }

    @Override
    public boolean updateUsers() {
        return false;
    }

    @Override
    public boolean deleteUsers() {
        return false;
    }

    @Override
    public boolean listCustomFields() {
        return false;
    }

    @Override
    public boolean addCustomFields() {
        return false;
    }

    @Override
    public boolean updateCustomFields() {
        return false;
    }

    @Override
    public boolean deleteCustomFields() {
        return false;
    }

    @Override
    public boolean listHistory() {
        return false;
    }

    @Override
    public boolean listProfiles() {
        return false;
    }
}
