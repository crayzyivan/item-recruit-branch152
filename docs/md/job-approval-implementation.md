# Job Approval System Implementation

## Overview
This document describes the implementation of a job approval process that allows admins to review, approve, reject, or request modifications to jobs created by subusers before they are posted publicly.

## Database Changes

### New Fields Added to `r_job` Table
- `submitted_for_approval_at` (TIMESTAMP) - When job was submitted for approval
- `approved_at` (TIMESTAMP) - When job was approved
- `approved_by` (BIGINT) - Who approved the job
- `denied_at` (TIMESTAMP) - When job was denied
- `denied_by` (BIGINT) - Who denied the job

### New Table: `r_job_audit_history`
- `id` (BIGINT, PRIMARY KEY)
- `job_id` (BIGINT, FOREIGN KEY)
- `old_status` (INT) - Previous job status
- `new_status` (INT) - New job status
- `action` (VARCHAR) - Action performed (APPROVE, REJECT, MODIFY, PUBLISH, ON_HOLD, CLOSE, STATUS_UPDATE)
- `comment` (TEXT) - Optional comment
- `created_by` (BIGINT) - User who performed the action
- `created_at` (TIMESTAMP)

**Note**: The `r_job_approval_comment` table was removed. All comments are now stored in `r_job_audit_history`.

### New Table: `r_job_approval_settings`
- `id` (BIGINT, PRIMARY KEY)
- `company_code` (VARCHAR) - Company code (unique)
- `approval_required` (BOOLEAN) - Whether approval is required for this company
- `email_notifications` (BOOLEAN) - Whether to send email notifications
- `admin_email` (VARCHAR) - Email address for admin notifications
- Standard audit fields (create/update time, user tracking)

## Job Statuses

### Complete Job Status Enum
- `DRAFT(0, "Draft")` - Job is in draft state
- `ACTIVE(1, "Active")` - Job is published and active
- `CLOSED(2, "Closed")` - Job is closed
- `OTHER(3, "Other")` - Other status
- `AWAITING_PAYMENT(4, "Awaiting Payment")` - Waiting for payment
- `ON_HOLD(5, "On Hold")` - Job is on hold
- `PENDING_APPROVAL(6, "Pending Review")` - Submitted for approval
- `PENDING_PUBLICATION(7, "Pending Publication")` - Approved, awaiting publication
- `PENDING_MODIFICATION(8, "Pending Modification")` - Rejected, requiring modifications

**Note**: Removed unused statuses `APPROVED`, `DENIED`, and `REQUEST_CHANGES` as they were not part of the actual workflow.

## New Components

### 1. JobAuditHistoryEntity
Entity class for storing complete audit history of job status changes and actions.

### 2. JobApprovalAction Enum
Enum defining job approval actions:
- `APPROVE` - Approve a job
- `REJECT` - Reject a job (covers both deny and request changes)
- `MODIFY` - Modify a job
- `PUBLISH` - Publish a job
- `ON_HOLD` - On Hold a job
- `CLOSE` - Close a job
- `RESUBMIT` - Resubmit for approval

### 3. JobApprovalService Interface
Service interface defining the job approval operations:
- `submitJobForApproval(Long jobId)` - Submit a job for approval
- `processJobApproval(JobApprovalRequestVO request)` - Process admin approval actions
- `getApprovalSettings(String companyCode)` - Get approval settings for a company
- `createApprovalSettings(JobApprovalSettingsVO settings)` - Create approval settings
- `updateApprovalSettings(JobApprovalSettingsVO settings)` - Update approval settings
- `getPendingApprovalJobs(String companyCode, int pageNo, int pageSize)` - Get pending approval jobs (MASTER_USER)
- `getAwaitingJobs(String companyCode, int pageNo, int pageSize)` - Get jobs awaiting user action (SUB_USER)
- `publishJob(Long jobId)` - Publish approved job
- `getJobAuditHistory(Long jobId)` - Get complete audit history for a job
- `sendApprovalNotification(Long jobId, String action, String comment, String email)` - Send email notifications

### 4. JobApprovalServiceImpl
Implementation of the job approval service with business logic for:
- Job approval workflow
- Status management and validation
- Audit history logging
- Email notifications with retry mechanism

### 5. JobApprovalController
REST controller providing endpoints for:
- `POST /job/approval/submit/{jobId}` - Submit job for approval (SUB_USER only)
- `POST /job/approval/process` - Process approval actions (MASTER_USER only)
- `GET /job/approval/settings` - Get approval settings (SUB_USER only)
- `POST /job/approval/settings` - Create approval settings (MASTER_USER only)
- `PUT /job/approval/settings` - Update approval settings (MASTER_USER only)
- `GET /job/approval/pending` - Get pending approval jobs (MASTER_USER only)
- `GET /job/approval/awaiting` - Get jobs awaiting user action (SUB_USER only)
- `POST /job/approval/publish/{jobId}` - Publish approved job (SUB_USER only)

### 6. JobController (Enhanced)
Additional endpoint added:
- `PUT /job/status` - Update job status with optional comment (SUB_USER only)

### 7. DTOs and VOs
- `JobApprovalRequestVO` - View object for approval requests (uses JobApprovalAction enum)
- `JobApprovalSettingsVO` - View object for approval settings
- `PendingJobListVO` - View object for pending job lists
- `JobAuditHistoryVO` - View object for audit history entries
- `JobStatusUpdateRequestVO` - View object for status updates with optional comment

### 8. Email Templates
HTML email templates for different approval scenarios:
- `job-approval-request.html` - Admin notification for new approval requests
- `job-approved.html` - Subuser notification for approved jobs
- `job-denied.html` - Subuser notification for rejected jobs
- `job-changes-requested.html` - Subuser notification for change requests

## Integration Points

### 1. Job Creation Workflow
Modified `JobDomainServiceImpl.publishJob()` method to:
- Check if approval is required for the company
- Set appropriate job status (PENDING_APPROVAL or ACTIVE)
- Send email notifications when approval is required
- Log audit history

### 2. Job Update Workflow
Modified `JobDomainServiceImpl.updateJob()` method to:
- Check if job status is PENDING_MODIFICATION
- Check if approval is required for the company
- Automatically resubmit for approval if both conditions are met
- Send notification to admin for re-review
- Log audit history

### 3. Job Status Updates
Modified `JobDomainServiceImpl.updateJobStatus()` method to:
- Validate status transitions
- Support optional comments for audit tracking
- Log all status changes to audit history
- Enforce PRD-defined status transition rules

### 4. Existing Infrastructure
Leverages existing components:
- MyBatis Plus for database operations
- Existing MailUtils for email notifications
- Current authentication system
- Company code system for multi-tenant support
- IAM service for fetching user information

## Workflow

### 1. Job Submission
1. Subuser creates and submits a job
2. System checks if approval is required for the company
3. If approval required: job status set to PENDING_APPROVAL, admin notified
4. If no approval required: job status set to ACTIVE, job published immediately

### 2. Admin Review
1. Admin receives email notification
2. Admin reviews job through approval dashboard
3. Admin can approve, reject, or request changes
4. Admin can add comments explaining their decision
5. All actions are logged to audit history

### 3. Job Status Updates
- **Approve**: Status changes to PENDING_PUBLICATION, subuser can publish
- **Reject**: Status changes to PENDING_MODIFICATION, subuser can modify and resubmit

### 4. Subuser Notifications
- Subusers receive appropriate email notifications based on admin actions
- Notifications include job details and admin comments
- Subusers can edit jobs after requested changes - resubmission happens automatically

### 5. Resubmission Workflows

#### Automatic Resubmission (Primary Workflow)
- When admin rejects job, status becomes `PENDING_MODIFICATION`
- When subuser updates the job, system automatically:
  - Checks if job status is `PENDING_MODIFICATION`
  - Checks if approval is required for company
  - If both true: automatically resubmits for approval
  - Sends notification to admin
  - Logs audit history

#### Manual Resubmission (Edge Cases)
- Use `POST /job/approval/submit/{jobId}` for:
  - Resubmitting rejected jobs without changes
  - Submitting draft jobs for approval
  - Requesting re-review of any job

### 6. Status Transition Rules
The system enforces PRD-defined status transition rules:
- PENDING_APPROVAL → PENDING_PUBLICATION (via APPROVE)
- PENDING_APPROVAL → PENDING_MODIFICATION (via REJECT)
- PENDING_PUBLICATION → ACTIVE (via PUBLISH)
- ACTIVE → ON_HOLD (via ON_HOLD)
- ACTIVE → CLOSED (via CLOSE)
- ON_HOLD → ACTIVE (via PUBLISH)
- CLOSED → ACTIVE (via PUBLISH)
- And more based on PRD requirements

## Audit History System

### Features
- Complete audit trail of all job status changes
- Tracks user who performed each action
- Stores old and new status for every transition
- Optional comments for each action
- Immutable history (no updates/deletes)

### Usage
- `GET /job/approval/history/{jobId}` - Retrieve complete audit history for a job
- Returns chronological list of all status changes and actions
- Each entry includes: user info, old status, new status, action, comment, timestamp

## Configuration

### Company-Level Settings
- `approvalRequired`: Toggle approval workflow on/off per company
- `emailNotifications`: Enable/disable email notifications (when disabled, no emails are sent)
- `adminEmail`: Email address for admin notifications (required for email notifications to work)

## Security and Permissions
- **Role-based access control**: SUB_USER can submit jobs, MASTER_USER can approve/manage
- **Company isolation**: Users can only access jobs from their own company
- **Service layer validation**: All operations validate company code ownership
- **Audit logging**: All approval actions are logged with user context
- **Status transition validation**: Invalid transitions are rejected

## Testing
Basic unit tests created for:
- Approval settings retrieval and updates
- Service method validation
- Duplicate title validation
- End-to-end workflow testing script (`test-job-approval-workflow.sh`)

## Deployment
1. Run Liquibase migration to create database schema
2. Deploy updated application code
3. Configure approval settings per company as needed

## Future Enhancements
- Admin dashboard UI for managing approvals
- Bulk approval operations
- Approval workflow customization
- Integration with external HR systems
- Advanced notification preferences