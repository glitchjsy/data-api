import { Company } from "./Company";

export interface Carpark {
    id: string;
    createdAt: string;
    updatedAt: string;
    name: string;
    payByPhoneCode: string | null;
    owner: Company | null;
    type: CarparkType;
    surfaceType: CarparkSurfaceType;
    multiStorey: boolean;
    latitude: number;
    longitude: number;
    paymentMethods: CarparkPaymentMethod[];
    spaces: number;
    disabledSpaces: number;
    parentChildSpaces: number;
    electricChargingSpaces: number;
    liveTrackingCode: string | null;
    notes: string;
}

export enum CarparkType {
    SHORT_STAY = "SHORT_STAY",
    LONG_STAY = "LONG_STAY",
    UNKNOWN = "UNKNOWN"
}

export enum CarparkSurfaceType {
    TARMAC = "TARMAC",
    CONCRETE = "CONCRETE",
    GRAVEL = "GRAVEL"
}

export enum CarparkPaymentMethod {
    PAY_BY_PHONE = "PAY_BY_PHONE",
    PAYCARD = "PAYCARD",
    SEASON_TICKET = "SEASON_TICKET",
    PAY_STATION = "PAY_STATION"
}