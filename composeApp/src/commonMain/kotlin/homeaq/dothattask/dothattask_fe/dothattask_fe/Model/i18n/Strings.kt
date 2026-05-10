package homeaq.dothattask.dothattask_fe.dothattask_fe.Model.i18n

/**
 * All localized strings used by the localized surfaces. Keep keys grouped
 * by screen so adding a new screen is just appending a new section.
 *
 * Two concrete instances live in [EnglishStrings] and [ItalianStrings];
 * the active one is selected at runtime by [LocaleManager] and delivered
 * to composables through the `LocalStrings` CompositionLocal.
 */
data class Strings(
    // Common
    val ok: String,
    val cancel: String,
    val back: String,
    val save: String,
    val delete: String,
    val skip: String,
    val next: String,
    val close: String,
    val retry: String,
    val loading: String,
    val unauthorized: String,
    val forbidden: String,
    val connectionError: String,

    // Onboarding (pre-login)
    val onboardingStep1Title: String,
    val onboardingStep1Body: String,
    val onboardingStep2Title: String,
    val onboardingStep2Body: String,
    val onboardingStep3Title: String,
    val onboardingStep3Body: String,
    val onboardingStep4Title: String,
    val onboardingStep4Body: String,
    val onboardingStart: String,
    val onboardingHaveAccount: String,

    // Feature tour (post-login)
    val featureTourHeader: String,
    val featureTourGo: String,
    val featureTour1Title: String,
    val featureTour1Body: String,
    val featureTour1Highlight: String,
    val featureTour2Title: String,
    val featureTour2Body: String,
    val featureTour2Highlight: String,
    val featureTour3Title: String,
    val featureTour3Body: String,
    val featureTour3Highlight: String,
    val featureTour4Title: String,
    val featureTour4Body: String,
    val featureTour4Highlight: String,
    val featureTour5Title: String,
    val featureTour5Body: String,
    val featureTour5Highlight: String,

    // Login
    val loginWelcome: String,
    val loginTaglineLine1: String,
    val loginTaglineLine2: String,
    val loginEmail: String,
    val loginPassword: String,
    val loginButton: String,
    val loginNoAccount: String,
    val loginEmailEmpty: String,
    val loginEmailTooLong: String,
    val loginEmailInvalid: String,
    val loginPasswordEmpty: String,
    val loginUnauthorized: String,
    val loginEndpointUnavailable: String,
    val loginServerError: String,
    val loginFailed: String,

    // Register
    val registerTitle: String,
    val registerName: String,
    val registerEmail: String,
    val registerUsernameOptional: String,
    val registerUsernameHint: String,
    val registerPassword: String,
    val registerButton: String,
    val registerHaveAccount: String,
    val registerNameEmpty: String,
    val registerEmailEmpty: String,
    val registerEmailInvalid: String,
    val registerPasswordEmpty: String,
    val registerPasswordTooShort: String,
    val registerEmailTaken: String,
    val registerUsernameTaken: String,
    val registerInvalidEmail: String,
    val registerSuccess: String,

    // Bottom navigation
    val navHome: String,
    val navManage: String,
    val navCompleted: String,
    val navGroups: String,
    val navInvites: String,

    // App scaffold / header
    val headerSettings: String,
    val headerLogout: String,
    val headerLanguage: String,
    val headerLanguageEnglish: String,
    val headerLanguageItalian: String,
    val headerChangePassword: String,

    // Page titles (mirrored in AppState.changePage)
    val titleLogin: String,
    val titleRegister: String,
    val titleChangePassword: String,
    val titleHome: String,
    val titleGroupHome: String,
    val titleInvitations: String,
    val titleInviteMember: String,
    val titleManageTasks: String,
    val titleCompleted: String,
    val titleSettings: String,

    // Settings page section labels
    val settingsSectionAccount: String,
    val settingsSectionPreferences: String,
    val settingsSectionSession: String,

    // GroupHome
    val groupsTitle: String,
    val groupsCreateGroup: String,
    val groupsOwnedBy: String,
    val groupsMembers: String,
    val groupsRoleOwner: String,
    val groupsRoleAdmin: String,
    val groupsRoleMember: String,
    val groupsInviteMember: String,
    val groupsLeave: String,
    val groupsCategories: String,
    val groupsNewCategory: String,

    // Invite Member
    val inviteTitle: String,
    val inviteIntoGroup: String,
    val inviteHint: String,
    val inviteEmail: String,
    val inviteSendButton: String,
    val inviteEmpty: String,
    val inviteEmailTooLong: String,
    val inviteEmailInvalid: String,
    val inviteSuccess: String,
    val inviteNoGroupSelected: String,
    val inviteFailed: String,

    // Incoming Invites
    val incomingTitle: String,
    val incomingEmpty: String,
    val incomingFromGroup: String,
    val incomingInvitedBy: String,
    val incomingAccept: String,
    val incomingReject: String,

    // Change password
    val changePasswordTitle: String,
    val changePasswordOld: String,
    val changePasswordNew: String,
    val changePasswordConfirm: String,
    val changePasswordSubmit: String,
    val changePasswordSuccess: String,
    val changePasswordOldEmpty: String,
    val changePasswordNewEmpty: String,
    val changePasswordMismatch: String,
    val changePasswordWrongOld: String,
    val changePasswordTooShort: String,

    // No group / invites empty states
    val noGroupHeadline: String,
    val noGroupBody: String,
    val noGroupCreate: String,
    val noGroupNamePlaceholder: String,
    val noGroupNameEmpty: String,

    // Error page
    val errorTitle: String,
    val errorRetry: String,
    val completedTasksButton: String,
    val completeTaskButton: String,
    val pickTask: String,
    val group: String,
    val taskDescription: String,
    val taskDescriptionEmpty: String,
    val activeGroup: String,
    val createTaskButton: String,
    val searchButtonParameter: String,
    val taskAssignee: String,
    val taskCategory: String,
    val groupPageEmptyState: String,
    val anyUser: String,
    val unauthorizedError: String?,
    val forbiddenError: String?,
    val updated: String,
    val created: String,
    val taskDisconnected: String?,
    val taskSearchEmptyState: String,
    val detailButton: String,
    val updateButton: String,
    val unassignButton: String,
    val addCategoryButton: String

)
