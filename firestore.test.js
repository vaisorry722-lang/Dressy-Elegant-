const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const OWNER_UID = "owner_dhaka_01";
const ACCOUNTANT_UID = "acc_dhaka_02";
const KARIGOR_UID = "karigor_dhaka_03";
const OUTSIDER_UID = "outsider_99";
const COMPANY_ID = "comp_dressy_001";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

async function seedCompanyAndMembers() {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const ts = new Date(Date.now() - 10000);
    const members = [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID];
    await db.collection("companies").doc(COMPANY_ID).set({
      companyCode: "DE-102030",
      name: "Dhaka Abaya Atelier",
      ownerId: OWNER_UID,
      ownerName: "Tariq Hasan",
      phone: "+8801711000000",
      email: "owner@dhakaabaya.bd",
      address: "Mirpur 10, Dhaka",
      category: "ABAYA_BUSINESS",
      country: "Bangladesh",
      currency: "BDT",
      timezone: "Asia/Dhaka",
      memberUids: members,
      subscriptionTier: "FREE",
      subscriptionPlan: "FREE",
      subscriptionStatus: "ACTIVE",
      paymentNumber: "301620556052",
      employeeCount: 2,
      karigorCount: 1,
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    });
    await db.collection("companies").doc(COMPANY_ID).collection("members").doc(OWNER_UID).set({
      companyId: COMPANY_ID,
      userId: OWNER_UID,
      displayName: "Tariq Hasan",
      email: "owner@dhakaabaya.bd",
      role: "COMPANY_OWNER",
      staffType: "OWNER",
      status: "ACTIVE",
      memberUids: members,
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    });
    await db.collection("companies").doc(COMPANY_ID).collection("members").doc(ACCOUNTANT_UID).set({
      companyId: COMPANY_ID,
      userId: ACCOUNTANT_UID,
      displayName: "Nadia Rahman",
      role: "ACCOUNTANT",
      staffType: "STAFF",
      status: "ACTIVE",
      memberUids: members,
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    });
    await db.collection("companies").doc(COMPANY_ID).collection("members").doc(KARIGOR_UID).set({
      companyId: COMPANY_ID,
      userId: KARIGOR_UID,
      displayName: "Kamal Mia",
      role: "KARIGOR",
      staffType: "KARIGOR",
      status: "ACTIVE",
      memberUids: members,
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    });
  });
}

test("Unauthenticated users are blocked from all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(OWNER_UID).get());
  await assertFails(unauthDb.collection("companies").where("memberUids", "array-contains", OWNER_UID).get());
  await assertFails(unauthDb.collection("companies").doc(COMPANY_ID).get());
});

test("User profile isolation and strict schema validation", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const outsiderDb = testEnv.authenticatedContext(OUTSIDER_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  await assertSucceeds(
    ownerDb.collection("users").doc(OWNER_UID).set({
      userId: OWNER_UID,
      displayName: "Tariq Hasan",
      email: "tariq@dressyelegant.bd",
      preferredLanguage: "bn",
      pinEnabled: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Partial update on PIN toggle and language
  await assertSucceeds(
    ownerDb.collection("users").doc(OWNER_UID).update({
      pinEnabled: true,
      preferredLanguage: "en",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Outsider cannot read owner PII
  await assertFails(outsiderDb.collection("users").doc(OWNER_UID).get());

  // Ghost field rejected
  await assertFails(
    ownerDb.collection("users").doc(OWNER_UID).update({
      isSuperAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Atomic company creation, owner member bootstrap, and client-side premium lock", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  // Atomic batch write mirroring CompanyRepository.createCompany()
  const batch = ownerDb.batch();
  const compRef = ownerDb.collection("companies").doc(COMPANY_ID);
  const memberRef = compRef.collection("members").doc(OWNER_UID);
  const logRef = compRef.collection("audit_logs").doc("log_init");

  batch.set(compRef, {
    companyCode: "DE-884210",
    name: "Noor Burqa House",
    ownerId: OWNER_UID,
    ownerName: "Tariq Hasan",
    phone: "+8801711223344",
    email: "info@noorburqa.bd",
    address: "Islampur, Dhaka",
    category: "BURQA_MANUFACTURING",
    country: "Bangladesh",
    currency: "BDT",
    timezone: "Asia/Dhaka",
    memberUids: [OWNER_UID],
    subscriptionTier: "FREE",
    subscriptionPlan: "FREE",
    subscriptionStatus: "ACTIVE",
    paymentNumber: "301620556052",
    employeeCount: 1,
    karigorCount: 0,
    createdBy: OWNER_UID,
    createdAt: ts,
    updatedAt: ts,
  });

  batch.set(memberRef, {
    companyId: COMPANY_ID,
    userId: OWNER_UID,
    displayName: "Tariq Hasan",
    email: "info@noorburqa.bd",
    role: "COMPANY_OWNER",
    staffType: "OWNER",
    status: "ACTIVE",
    memberUids: [OWNER_UID],
    createdBy: OWNER_UID,
    createdAt: ts,
    updatedAt: ts,
  });

  batch.set(logRef, {
    companyId: COMPANY_ID,
    memberUids: [OWNER_UID],
    actorUid: OWNER_UID,
    actorName: "Tariq Hasan",
    action: "COMPANY_CREATED",
    details: "Workspace initialized",
    createdBy: OWNER_UID,
    createdAt: ts,
  });

  await assertSucceeds(batch.commit());

  // Filtered list query succeeds, unfiltered list fails
  await assertSucceeds(
    ownerDb.collection("companies").where("memberUids", "array-contains", OWNER_UID).get()
  );
  await assertFails(ownerDb.collection("companies").get());

  // Client cannot self-promote to PREMIUM subscriptionTier
  await assertFails(
    ownerDb.collection("companies").doc(COMPANY_ID).update({
      subscriptionTier: "PREMIUM",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Multi-tenant isolation and RBAC enforcement", async () => {
  await seedCompanyAndMembers();

  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const accountantDb = testEnv.authenticatedContext(ACCOUNTANT_UID).firestore();
  const karigorDb = testEnv.authenticatedContext(KARIGOR_UID).firestore();
  const outsiderDb = testEnv.authenticatedContext(OUTSIDER_UID).firestore();
  const ts = new Date(Date.now() - 1000);

  // Outsider blocked from company and subcollections
  await assertFails(outsiderDb.collection("companies").doc(COMPANY_ID).get());
  await assertFails(outsiderDb.collection("companies").doc(COMPANY_ID).collection("transactions").doc("tx1").get());

  // Accountant can create & read financial transactions
  await assertSucceeds(
    accountantDb.collection("companies").doc(COMPANY_ID).collection("transactions").doc("tx_1").set({
      companyId: COMPANY_ID,
      memberUids: [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID],
      entryType: "INCOME",
      category: "Wholesale Order",
      title: "50 Black Dubai Abayas",
      amountBdt: 85000,
      paymentMethod: "BANK",
      createdBy: ACCOUNTANT_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Karigor CANNOT read single financial doc or create financial transactions
  await assertFails(
    karigorDb.collection("companies").doc(COMPANY_ID).collection("transactions").doc("tx_1").get()
  );
  await assertFails(
    karigorDb.collection("companies").doc(COMPANY_ID).collection("transactions").doc("tx_2").set({
      companyId: COMPANY_ID,
      memberUids: [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID],
      entryType: "EXPENSE",
      category: "Other",
      title: "Unauthorized",
      amountBdt: 500,
      paymentMethod: "CASH",
      createdBy: KARIGOR_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // SUPER_ADMIN cannot be assigned by anyone
  await assertFails(
    ownerDb.collection("companies").doc(COMPANY_ID).collection("members").doc("hacker_01").set({
      companyId: COMPANY_ID,
      userId: "hacker_01",
      displayName: "Escalated",
      role: "SUPER_ADMIN",
      staffType: "STAFF",
      status: "ACTIVE",
      memberUids: [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID],
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Payment request must target 301620556052 and status PENDING_VERIFICATION
  await assertSucceeds(
    ownerDb.collection("companies").doc(COMPANY_ID).collection("payment_requests").doc("pay_1").set({
      companyId: COMPANY_ID,
      memberUids: [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID],
      plan: "MONTHLY",
      amountBdt: 1490,
      paymentNumber: "301620556052",
      senderAccount: "01711000000",
      transactionRef: "BKX982173",
      status: "PENDING_VERIFICATION",
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    ownerDb.collection("companies").doc(COMPANY_ID).collection("payment_requests").doc("pay_2").set({
      companyId: COMPANY_ID,
      memberUids: [OWNER_UID, ACCOUNTANT_UID, KARIGOR_UID],
      plan: "MONTHLY",
      amountBdt: 1490,
      paymentNumber: "301620556052",
      senderAccount: "01711000000",
      transactionRef: "BKX982174",
      status: "APPROVED",
      createdBy: OWNER_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});
