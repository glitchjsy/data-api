export interface Company {
    id: string;
    createdAt: string;
    updatedAt: string;
    name: string;
    address: string | null;
    emailAddress: string | null;
    phoneNumber: string | null;
    websiteUrl: string | null;
}