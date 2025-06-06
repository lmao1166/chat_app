const message = require('../models/message.model');
const { Message, User, Conversation } = require('../models');

class MessageRepository {
    async findAll() {
        try {
            const messages = await message.findAll({
                include: [
                    {
                        model: User,
                        as: 'sender',
                        attributes: ['id', 'username', 'email']
                    },
                    {
                        model: Conversation,
                        as: 'conversation',
                        attributes: ['id', 'type', 'name']
                    }
                ],
                order: [['timestamp', 'DESC']]
            });
            return messages;
        } catch (error) {
            throw new Error('Error fetching messages: ' + error.message);
        }
    }

    async findById(id) {
        try {
            const messageRecord = await message.findByPk(id, {
                include: [
                    {
                        model: User,
                        as: 'sender',
                        attributes: ['id', 'username', 'profilePicUrl']
                    },
                    {
                        model: Conversation,
                        as: 'conversation',
                        attributes: ['id', 'type', 'name']
                    }
                ]
            });
            return messageRecord;
        } catch (error) {
            throw new Error('Error fetching message: ' + error.message);
        }
    }

    async findByConversationId(conversationId, options = {}) {
        try {
            const defaultOptions = {
                where: { conversation_id: conversationId },
                include: [
                    {
                        model: User,
                        as: 'sender',
                        attributes: ['id', 'username', 'email', "profilePicUrl"]
                    }
                ],
                order: [['timestamp', 'ASC']]
            };

            const mergedOptions = { ...defaultOptions, ...options };
            const messages = await message.findAll(mergedOptions);
            return messages;
        } catch (error) {
            throw new Error('Error fetching messages by conversation: ' + error.message);
        }
    }

    async create(messageData) {
        const transaction = await message.sequelize.transaction();
        try {
            const newMessage = await message.create(messageData, { transaction });
            await transaction.commit();
            
            return newMessage;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating message: ' + error.message);
        }
    }    async update(id, messageData) {
        const transaction = await message.sequelize.transaction();
        try {
            const [updatedRows] = await message.update(messageData, {
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error updating message: ' + error.message);
        }
    }    async delete(id) {
        const transaction = await message.sequelize.transaction();
        try {
            const deletedRows = await message.destroy({
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting message: ' + error.message);
        }
    }    async markAsDeleted(id, userId) {
        const transaction = await message.sequelize.transaction();
        try {
            const [updatedRows] = await message.update(
                { deleted_by_sender: true },
                {
                    where: { 
                        id: id,
                        sender_id: userId
                    },
                    transaction: transaction
                }
            );
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error marking message as deleted: ' + error.message);
        }
    }
}

module.exports = new MessageRepository();