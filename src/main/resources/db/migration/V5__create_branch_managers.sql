-- manager_id points to a staff member who has role BRANCH_MANAGER
ALTER TABLE branches
    ADD COLUMN manager_id UUID REFERENCES staff(id);