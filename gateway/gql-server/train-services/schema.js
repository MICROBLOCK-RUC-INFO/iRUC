const { gql } = require('apollo-server');
const {
  Trip,
  FoodOrder,
  TrainFood,
  Route,
  StationFoodStore,
  FoodDeliveryOrder,
} = require('./db');

// GraphQL类型定义
const typeDefs = gql`
  # TripId类型
  type TripId {
    type: String!
    number: String!
  }

  input TripIdInput {
    type: String!
    number: String!
  }

  # Food类型
  type Food {
    foodName: String!
    price: Float!
  }

  input FoodInput {
    foodName: String!
    price: Float!
  }

  # Trip类型
  type Trip {
    id: ID!
    tripId: TripId!
    trainTypeName: String!
    routeId: String!
    startStationName: String!
    stationsName: String
    terminalStationName: String!
    startTime: String!
    endTime: String!
  }

  # FoodOrder类型
  type FoodOrder {
    id: ID!
    orderId: String!
    foodType: Int!
    stationName: String!
    storeName: String!
    foodName: String!
    price: Float!
  }

  # TrainFood类型
  type TrainFood {
    id: ID!
    tripId: TripId!
    foodList: [Food!]!
  }

  # Route类型
  type Route {
    id: ID!
    stations: [String!]!
    distances: [Int!]!
    startStation: String!
    endStation: String!
  }

  # StationFoodStore类型
  type StationFoodStore {
    id: ID!
    stationName: String!
    storeName: String!
    telephone: String!
    businessTime: String!
    deliveryFee: Float!
    foodList: [Food!]!
  }

  # FoodDeliveryOrder类型
  type FoodDeliveryOrder {
    id: ID!
    stationFoodStoreId: ID!
    foodList: [Food!]!
    tripId: String!
    seatNo: Int!
    createdTime: String!
    deliveryTime: String!
    deliveryFee: Float!
  }

  # 查询
  type Query {
    # Trip查询
    getAllTrips: [Trip!]!
    getTripById(id: ID!): Trip
    getTripsByRouteId(routeId: String!): [Trip!]!

    # FoodOrder查询
    getAllFoodOrders: [FoodOrder!]!
    getFoodOrderById(id: ID!): FoodOrder
    getFoodOrdersByStation(stationName: String!): [FoodOrder!]!

    # TrainFood查询
    getAllTrainFoods: [TrainFood!]!
    getTrainFoodById(id: ID!): TrainFood
    getTrainFoodByTripId(tripType: String!, tripNumber: String!): TrainFood

    # Route查询
    getAllRoutes: [Route!]!
    getRouteById(id: ID!): Route

    # StationFoodStore查询
    getAllStationFoodStores: [StationFoodStore!]!
    getStationFoodStoreById(id: ID!): StationFoodStore
    getStationFoodStoresByStation(stationName: String!): [StationFoodStore!]!

    # FoodDeliveryOrder查询
    getAllFoodDeliveryOrders: [FoodDeliveryOrder!]!
    getFoodDeliveryOrderById(id: ID!): FoodDeliveryOrder
    getFoodDeliveryOrdersByTripId(tripId: String!): [FoodDeliveryOrder!]!
  }

  # 输入类型
  input TripInput {
    tripId: TripIdInput!
    trainTypeName: String!
    routeId: String!
    startStationName: String!
    stationsName: String
    terminalStationName: String!
    startTime: String!
    endTime: String!
  }

  input FoodOrderInput {
    orderId: String!
    foodType: Int!
    stationName: String!
    storeName: String!
    foodName: String!
    price: Float!
  }

  input TrainFoodInput {
    tripId: TripIdInput!
    foodList: [FoodInput!]!
  }

  input RouteInput {
    id: ID!
    stations: [String!]!
    distances: [Int!]!
    startStation: String!
    endStation: String!
  }

  input StationFoodStoreInput {
    stationName: String!
    storeName: String!
    telephone: String!
    businessTime: String!
    deliveryFee: Float!
    foodList: [FoodInput!]!
  }

  input FoodDeliveryOrderInput {
    stationFoodStoreId: ID!
    foodList: [FoodInput!]!
    tripId: String!
    seatNo: Int!
    createdTime: String!
    deliveryTime: String!
    deliveryFee: Float!
  }

  # 变更
  type Mutation {
    # Trip变更
    createTrip(input: TripInput!): Trip!
    updateTrip(id: ID!, input: TripInput!): Trip
    deleteTrip(id: ID!): Boolean!

    # FoodOrder变更
    createFoodOrder(input: FoodOrderInput!): FoodOrder!
    updateFoodOrder(id: ID!, input: FoodOrderInput!): FoodOrder
    deleteFoodOrder(id: ID!): Boolean!

    # TrainFood变更
    createTrainFood(input: TrainFoodInput!): TrainFood!
    updateTrainFood(id: ID!, input: TrainFoodInput!): TrainFood
    deleteTrainFood(id: ID!): Boolean!

    # Route变更
    createRoute(input: RouteInput!): Route!
    updateRoute(id: ID!, input: RouteInput!): Route
    deleteRoute(id: ID!): Boolean!

    # StationFoodStore变更
    createStationFoodStore(input: StationFoodStoreInput!): StationFoodStore!
    updateStationFoodStore(id: ID!, input: StationFoodStoreInput!): StationFoodStore
    deleteStationFoodStore(id: ID!): Boolean!

    # FoodDeliveryOrder变更
    createFoodDeliveryOrder(input: FoodDeliveryOrderInput!): FoodDeliveryOrder!
    updateFoodDeliveryOrder(id: ID!, input: FoodDeliveryOrderInput!): FoodDeliveryOrder
    deleteFoodDeliveryOrder(id: ID!): Boolean!
  }
`;

// GraphQL解析器定义
const resolvers = {
  Query: {
    // Trip查询解析器
    getAllTrips: async () => await Trip.findAll(),
    getTripById: async (_, { id }) => await Trip.findByPk(id),
    getTripsByRouteId: async (_, { routeId }) => 
      await Trip.findAll({ where: { routeId } }),

    // FoodOrder查询解析器
    getAllFoodOrders: async () => await FoodOrder.findAll(),
    getFoodOrderById: async (_, { id }) => await FoodOrder.findByPk(id),
    getFoodOrdersByStation: async (_, { stationName }) => 
      await FoodOrder.findAll({ where: { stationName } }),

    // TrainFood查询解析器
    getAllTrainFoods: async () => await TrainFood.findAll(),
    getTrainFoodById: async (_, { id }) => await TrainFood.findByPk(id),
    getTrainFoodByTripId: async (_, { tripType, tripNumber }) => {
      const trainFood = await TrainFood.findOne({
        where: {
          tripId: {
            type: tripType,
            number: tripNumber,
          },
        },
      });
      return trainFood;
    },

    // Route查询解析器
    getAllRoutes: async () => await Route.findAll(),
    getRouteById: async (_, { id }) => await Route.findByPk(id),

    // StationFoodStore查询解析器
    getAllStationFoodStores: async () => await StationFoodStore.findAll(),
    getStationFoodStoreById: async (_, { id }) => await StationFoodStore.findByPk(id),
    getStationFoodStoresByStation: async (_, { stationName }) => 
      await StationFoodStore.findAll({ where: { stationName } }),

    // FoodDeliveryOrder查询解析器
    getAllFoodDeliveryOrders: async () => await FoodDeliveryOrder.findAll(),
    getFoodDeliveryOrderById: async (_, { id }) => await FoodDeliveryOrder.findByPk(id),
    getFoodDeliveryOrdersByTripId: async (_, { tripId }) => 
      await FoodDeliveryOrder.findAll({ where: { tripId } }),
  },

  Mutation: {
    // Trip变更解析器
    createTrip: async (_, { input }) => await Trip.create(input),
    updateTrip: async (_, { id, input }) => {
      await Trip.update(input, { where: { id } });
      return await Trip.findByPk(id);
    },
    deleteTrip: async (_, { id }) => {
      const result = await Trip.destroy({ where: { id } });
      return result > 0;
    },

    // FoodOrder变更解析器
    createFoodOrder: async (_, { input }) => await FoodOrder.create(input),
    updateFoodOrder: async (_, { id, input }) => {
      await FoodOrder.update(input, { where: { id } });
      return await FoodOrder.findByPk(id);
    },
    deleteFoodOrder: async (_, { id }) => {
      const result = await FoodOrder.destroy({ where: { id } });
      return result > 0;
    },

    // TrainFood变更解析器
    createTrainFood: async (_, { input }) => await TrainFood.create(input),
    updateTrainFood: async (_, { id, input }) => {
      await TrainFood.update(input, { where: { id } });
      return await TrainFood.findByPk(id);
    },
    deleteTrainFood: async (_, { id }) => {
      const result = await TrainFood.destroy({ where: { id } });
      return result > 0;
    },

    // Route变更解析器
    createRoute: async (_, { input }) => await Route.create(input),
    updateRoute: async (_, { id, input }) => {
      await Route.update(input, { where: { id } });
      return await Route.findByPk(id);
    },
    deleteRoute: async (_, { id }) => {
      const result = await Route.destroy({ where: { id } });
      return result > 0;
    },

    // StationFoodStore变更解析器
    createStationFoodStore: async (_, { input }) => 
      await StationFoodStore.create(input),
    updateStationFoodStore: async (_, { id, input }) => {
      await StationFoodStore.update(input, { where: { id } });
      return await StationFoodStore.findByPk(id);
    },
    deleteStationFoodStore: async (_, { id }) => {
      const result = await StationFoodStore.destroy({ where: { id } });
      return result > 0;
    },

    // FoodDeliveryOrder变更解析器
    createFoodDeliveryOrder: async (_, { input }) => 
      await FoodDeliveryOrder.create(input),
    updateFoodDeliveryOrder: async (_, { id, input }) => {
      await FoodDeliveryOrder.update(input, { where: { id } });
      return await FoodDeliveryOrder.findByPk(id);
    },
    deleteFoodDeliveryOrder: async (_, { id }) => {
      const result = await FoodDeliveryOrder.destroy({ where: { id } });
      return result > 0;
    },
  },

  // 自定义字段解析器（如果需要）
  Trip: {
    tripId: (parent) => parent.tripId,
  },
  TrainFood: {
    tripId: (parent) => parent.tripId,
    foodList: (parent) => parent.foodList,
  },
  StationFoodStore: {
    foodList: (parent) => parent.foodList,
  },
  FoodDeliveryOrder: {
    foodList: (parent) => parent.foodList,
  },
};

module.exports = { typeDefs, resolvers };
