const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendGroupChatNotification = functions.database.ref("/Group Chat/{pushId}")
    .onCreate((snapshot, context) => {
        const messageData = snapshot.val();
        const senderId = messageData.uId;

        return admin.database().ref(`/Users/${senderId}`).once("value").then(userSnapshot => {
            const senderName = userSnapshot.val().userName;
            const messageText = messageData.message;

            const payload = {
                notification: {
                    title: `New message from ${senderName}`,
                    body: messageText,
                    icon: "ic_notification",
                    click_action: "GROUP_CHAT_ACTIVITY"
                }
            };

            return admin.messaging().sendToTopic("group_chat", payload);
        });
    });
