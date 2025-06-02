const user = require('../models/user.model');

class UserRepository {
    async findAll() {
        try {
            const users = await user.findAll();
            return users;
        } catch (error) {
            throw new Error('Error fetching users: ' + error.message);
        }
    }

    async findById(id) {
        try {
            const userRecord = await user.findByPk(id);
            return userRecord;
        } catch (error) {
            throw new Error('Error fetching user: ' + error.message);
        }
    }

    async findByEmail(email) {
        try {
            const userRecord = await user.findOne({ where: { email: email } });
            return userRecord;
        } catch (error) {
            throw new Error('Error fetching user by email: ' + error.message);
        }
    }

    async create(userData) {
        const transaction = await user.sequelize.transaction();
        try {
            userData.password = await bcrypt.hash(userData.password, 10);
            const newUser = await user.create(userData, { transaction });
            await transaction.commit();
            return newUser;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error creating user: ' + error.message);
        }
    }

    async update(id, userData) {
        const transaction = await user.sequelize.transaction();
        try {
            userData.password = await bcrypt.hash(userData.password, 10);
            const [updatedRows] = await user.update(userData, {
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return updatedRows;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error updating user: ' + error.message);
        }
    }

    async delete(id) {
        const transaction = await user.sequelize.transaction();
        try {
            const deletedRow = await user.destroy({
                where: { id: id },
                transaction: transaction
            });
            await transaction.commit();
            return deletedRow;
        } catch (error) {
            await transaction.rollback();
            throw new Error('Error deleting user: ' + error.message);
        }
    }
}

module.exports = new UserRepository();