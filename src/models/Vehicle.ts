export interface Vehicle {
    firstRegisteredAt: string;
    firstRegisteredInJerseyAt: string;
    make: string;
    model: string | null;
    type: VehicleType;
    color: string;
    cylinderCapacity: number | null;
    weight: string;
    co2Emissions: number | null;
    fuelType: VehicleFuelType;
    hash: string;
}

export interface VehicleWithLookupData extends Vehicle {
    previousOwners: number;
    previousTraders: number;
}

export enum VehicleFuelType {
    PETROL = "PETROL",
    HEAVY_OIL = "HEAVY_OIL",
    ELECTRIC = "ELECTRIC",
    HYBRID_ELECTRIC = "HYBRID_ELECTRIC",
    GAS = "GAS",
    DIESEL_ELECTRIC = "DIESEL_ELECTRIC",
    GAS_BI_FUEL = "GAS_BI_FUEL",
    STEAM = "STEAM",
    UNKNOWN = "UNKNOWN"
}

export enum VehicleType {
    CAR = "CAR",
    MOTORCYCLE = "MOTORCYCLE",
    AGRICULTURAL = "AGRICULTURAL",
    LIGHT_4X4_UTILITY = "LIGHT_4X4_UTILITY",
    LIGHT_COMMERCIAL = "LIGHT_COMMERCIAL",
    MEDIUM_WEIGHT = "MEDIUM_WEIGHT",
    HEAVY_WEIGHT = "HEAVY_WEIGHT",
    MOTOR_CARAVAN = "MOTOR_CARAVAN",
    MOTOR_DRAWN_TRAILER = "MOTOR_DRAWN_TRAILER",
    BUS = "BUS",
    CONSTRUCTION = "CONSTRUCTION",
    WORKS_TRUCK = "WORKS_TRUCK",
    UNKNOWN = "UNKNOWN"
}