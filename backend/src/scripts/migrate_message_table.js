const { sequelize } = require('../configs/database');

async function updateMessageTable() {
    try {
        console.log('Starting Message table migration...');
        
        // Kiểm tra xem cột đã tồn tại chưa trước khi thêm
        const [results] = await sequelize.query(`
            SELECT COLUMN_NAME 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_NAME = 'messages' 
            AND TABLE_SCHEMA = DATABASE()
        `);
        
        const existingColumns = results.map(row => row.COLUMN_NAME);
        
        // Thêm cột message_type nếu chưa tồn tại
        if (!existingColumns.includes('message_type')) {
            await sequelize.query(`
                ALTER TABLE messages 
                ADD COLUMN message_type ENUM('text', 'image', 'file', 'system') 
                NOT NULL DEFAULT 'text'
            `);
            console.log('✅ Added message_type column');
        } else {
            console.log('ℹ️ message_type column already exists');
        }
        
        // Thêm cột delivery_status nếu chưa tồn tại
        if (!existingColumns.includes('delivery_status')) {
            await sequelize.query(`
                ALTER TABLE messages 
                ADD COLUMN delivery_status ENUM('sent', 'delivered', 'failed') 
                NOT NULL DEFAULT 'sent'
            `);
            console.log('✅ Added delivery_status column');
        } else {
            console.log('ℹ️ delivery_status column already exists');
        }
        
        // Thêm cột is_deleted nếu chưa tồn tại
        if (!existingColumns.includes('is_deleted')) {
            await sequelize.query(`
                ALTER TABLE messages 
                ADD COLUMN is_deleted BOOLEAN 
                NOT NULL DEFAULT false
            `);
            console.log('✅ Added is_deleted column');
        } else {
            console.log('ℹ️ is_deleted column already exists');
        }
        
        console.log('✅ Message table migration completed successfully!');
        
    } catch (error) {
        console.error('❌ Error during migration:', error);
        throw error;
    }
}

// Chạy migration nếu file được execute trực tiếp
if (require.main === module) {
    updateMessageTable()
        .then(() => {
            console.log('Migration completed');
            process.exit(0);
        })
        .catch((error) => {
            console.error('Migration failed:', error);
            process.exit(1);
        });
}

module.exports = { updateMessageTable };