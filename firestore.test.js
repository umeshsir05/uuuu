const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

test("Unauthenticated user cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      defaultRate: 60,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Alice can create her own profile and milk entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      dairyName: "Radhey Dairy",
      contactPhone: "+919876543210",
      defaultRate: 60,
      cowRate: 60,
      buffaloRate: 75,
      upiId: "radhey@upi",
      language: "hi",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("entries").doc("entry_1").set({
      userId: ALICE_UID,
      date: "2026-10-20",
      shift: "MORNING",
      milkType: "COW",
      quantity: 1.5,
      rate: 60,
      totalAmount: 90,
      isSkipped: false,
      notes: "Fresh milk",
      createdAt: new Date(),
    })
  );
});

test("Bob cannot read Alice's milk entries", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("entries").doc("entry_1").set({
      userId: ALICE_UID,
      date: "2026-10-20",
      shift: "MORNING",
      quantity: 2.0,
      rate: 60,
      totalAmount: 120,
      isSkipped: false,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("entries").doc("entry_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("entries").get());
});

test("Alice can add payment and record rate history", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payments").doc("pay_1").set({
      userId: ALICE_UID,
      date: "2026-10-20",
      month: "2026-10",
      amount: 1500,
      paymentMethod: "UPI",
      notes: "Advance payment",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("rate_history").doc("rate_1").set({
      userId: ALICE_UID,
      effectiveDate: "2026-10-01",
      milkType: "COW",
      rate: 62,
      notes: "Festive season revision",
      createdAt: new Date(),
    })
  );
});
