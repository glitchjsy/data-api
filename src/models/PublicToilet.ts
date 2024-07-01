export interface PublicToilet {
    id: string;
    createdAt: string;
    updatedAt: string;
    name: string;
    parish: string;
    latitude: number;
    longitude: number;
    tenure: PublicToiletTenure;
    buildDate: number | null;
    facilities: PublicToiletFacilities[];
    owner: {
        id: number;
        name: string;
    }
    female: {
        cubicles: number | null;
        handDryers: number | null;
        sinks: number | null;
        periodProducts: PeriodProduct[]
    } | null;
    male: {
        cubicles: number | null;
        urinals: number | null;
        handDryers: number | null;
        sinks: number | null;
        periodProducts: PeriodProduct[]
    } | null;
}

export enum PeriodProduct {
    PADS = "PADS",
    TAMPONS = "TAMPONS"
}

export enum PeriodProductToiletType {
    MALE = "MALE",
    FEMALE = "FEMALE"
}

export enum PublicToiletTenure {
    FREEHOLD = "FREEHOLD",
    LEASEHOLD = "LEASEHOLD",
    PARISH = "PARISH"
}

export enum PublicToiletFacilities {
    GENDER_NEUTRAL = "GENDER_NEUTRAL",
    BEACH_SHOWER = "BEACH_SHOWER",
    BABY_CHANGING = "BABY_CHANGING",
    DISABLED = "DISABLED"
}