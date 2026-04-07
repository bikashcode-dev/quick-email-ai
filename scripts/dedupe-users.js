const dbName = "quickmail";
const database = db.getSiblingDB(dbName);

const duplicateGroups = database.users.aggregate([
  {
    $group: {
      _id: "$email",
      ids: { $push: "$_id" },
      count: { $sum: 1 },
      latestCreatedAt: { $max: "$createdAt" }
    }
  },
  {
    $match: {
      count: { $gt: 1 }
    }
  }
]).toArray();

print(`Found ${duplicateGroups.length} duplicate email group(s).`);

duplicateGroups.forEach((group) => {
  const users = database.users
    .find({ _id: { $in: group.ids } })
    .sort({ isVerified: -1, lastLoginAt: -1, createdAt: -1, _id: 1 })
    .toArray();

  const keeper = users[0];
  const duplicates = users.slice(1);

  duplicates.forEach((duplicate) => {
    database.users.deleteOne({ _id: duplicate._id });
    print(`Deleted duplicate user ${duplicate._id} for ${group._id}`);
  });

  print(`Kept user ${keeper._id} for ${group._id}`);
});

print("Duplicate cleanup complete.");
