const { Sequelize, DataTypes } = require('sequelize');

// 创建与PostgreSQL数据库的连接
const sequelize = new Sequelize('pg连接', {
  dialect: 'postgres',
  logging: false, // 设置为 true 可以看到 SQL 查询日志
});

// 定义 Trip 模型
const Trip = sequelize.define('Trip', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true,
  },
  tripId: {
    type: DataTypes.JSONB, // 存储 { type: "G", number: "123" }
    allowNull: false,
  },
  trainTypeName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  routeId: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  startStationName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  stationsName: {
    type: DataTypes.TEXT,
    allowNull: true,
  },
  terminalStationName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  startTime: {
    type: DataTypes.DATE,
    allowNull: false,
  },
  endTime: {
    type: DataTypes.DATE,
    allowNull: false,
  },
}, {
  tableName: 'trips',
  timestamps: false,
});

// 定义 FoodOrder 模型
const FoodOrder = sequelize.define('FoodOrder', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true,
  },
  orderId: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  foodType: {
    type: DataTypes.INTEGER,
    allowNull: false,
  },
  stationName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  storeName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  foodName: {
    type: DataTypes.TEXT,
    allowNull: false,
  },
  price: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
  },
}, {
  tableName: 'food_orders',
  timestamps: false,
});

// 定义 TrainFood 模型
const TrainFood = sequelize.define('TrainFood', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true,
  },
  tripId: {
    type: DataTypes.JSONB, // 存储 { type: "G", number: "123" }
    allowNull: false,
  },
  foodList: {
    type: DataTypes.JSONB, // 存储食品列表数组
    allowNull: false,
  },
}, {
  tableName: 'train_foods',
  timestamps: false,
});

// 定义 Route 模型
const Route = sequelize.define('Route', {
  id: {
    type: DataTypes.STRING,
    primaryKey: true,
  },
  stations: {
    type: DataTypes.ARRAY(DataTypes.TEXT),
    allowNull: false,
  },
  distances: {
    type: DataTypes.ARRAY(DataTypes.INTEGER),
    allowNull: false,
  },
  startStation: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  endStation: {
    type: DataTypes.STRING,
    allowNull: false,
  },
}, {
  tableName: 'routes',
  timestamps: false,
});

// 定义 StationFoodStore 模型
const StationFoodStore = sequelize.define('StationFoodStore', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true,
  },
  stationName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  storeName: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  telephone: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  businessTime: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  deliveryFee: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
  },
  foodList: {
    type: DataTypes.JSONB, // 存储食品列表数组
    allowNull: false,
  },
}, {
  tableName: 'station_food_stores',
  timestamps: false,
});

// 定义 FoodDeliveryOrder 模型
const FoodDeliveryOrder = sequelize.define('FoodDeliveryOrder', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true,
  },
  stationFoodStoreId: {
    type: DataTypes.UUID,
    allowNull: false,
  },
  foodList: {
    type: DataTypes.JSONB, // 存储食品列表数组
    allowNull: false,
  },
  tripId: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  seatNo: {
    type: DataTypes.INTEGER,
    allowNull: false,
  },
  createdTime: {
    type: DataTypes.DATE,
    allowNull: false,
  },
  deliveryTime: {
    type: DataTypes.DATE,
    allowNull: false,
  },
  deliveryFee: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false,
  },
}, {
  tableName: 'food_delivery_orders',
  timestamps: false,
});

module.exports = {
  sequelize,
  Trip,
  FoodOrder,
  TrainFood,
  Route,
  StationFoodStore,
  FoodDeliveryOrder,
};
