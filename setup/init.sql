CREATE TABLE `companies` (
    `id` varchar(40) DEFAULT (uuid()) NOT NULL,
    `createdAt` timestamp DEFAULT current_timestamp NOT NULL,
    `updatedAt` timestamp DEFAULT current_timestamp ON UPDATE current_timestamp NOT NULL,
    `name` varchar(50) NOT NULL,
    `address` varchar(100),
    `emailAddress` varchar(100),
    `phoneNumber` varchar(12),
    `websiteUrl` varchar(150),
    PRIMARY KEY (`id`)
);

CREATE TABLE `carparks` (
    `id` varchar(40) DEFAULT (uuid()),
    `createdAt` timestamp DEFAULT current_timestamp NOT NULL,
    `updatedAt` timestamp DEFAULT current_timestamp ON UPDATE current_timestamp NOT NULL,
    `name` varchar(50) NOT NULL,
    `payByPhoneCode` varchar(10),
    `ownerId` varchar(40),
    `type` varchar(20) NOT NULL,
    `surfaceType` varchar(20) NOT NULL,
    `multiStorey` boolean DEFAULT 0 NOT NULL,
    `latitude` decimal(10, 8),
    `longitude` decimal(11, 8),
    `spaces` int NOT NULL,
    `disabledSpaces` int DEFAULT 0 NOT NULL,
    `parentChildSpaces` int DEFAULT 0 NOT NULL,
    `electricChargingSpaces` int DEFAULT 0 NOT NULL,
    `liveTrackingCode` varchar(30),
    `notes` text,
    FOREIGN KEY (`ownerId`) REFERENCES `companies`(`id`) ON DELETE SET NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `carparkPaymentMethods` (
    `id` int auto_increment NOT NULL,
    `carparkId` varchar(40) NOT NULL,
    `paymentMethod` varchar(20) NOT NULL,
    FOREIGN KEY (`carparkId`) REFERENCES `carparks`(`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
);

CREATE TABLE `liveParkingSpaces` (
    `id` int auto_increment NOT NULL,
    `createdAt` timestamp NOT NULL,
    `name` varchar(30) NOT NULL,
    `code` varchar(20) NOT NULL,
    `spaces` int NOT NULL,
    `status` varchar(20) NOT NULL,
    `open` boolean NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `vehicles` (
    `id` int auto_increment NOT NULL,
    `firstRegisteredAt` date NOT NULL,
    `firstRegisteredInJerseyAt` date NOT NULL,
    `make` varchar(40) NOT NULL,
    `model` varchar(40),
    `color` varchar(40),
    `cylinderCapacity` int,
    `weight` varchar(30),
    `co2Emissions` int,
    `fuelType` varchar(30) NOT NULL,
    `hash` varchar(100) NOT NULL,
    PRIMARY KEY (`id`)
);

ALTER TABLE `vehicles` ADD INDEX `vehicle_hash_index` (`hash`);

CREATE TABLE `publicAccessDefibrillators` (
    `id` varchar(40) DEFAULT (uuid()),
    `createdAt` timestamp DEFAULT current_timestamp NOT NULL,
    `updatedAt` timestamp DEFAULT current_timestamp ON UPDATE current_timestamp NOT NULL,
    `location` varchar(80) NOT NULL,
    `parish` varchar(30) NOT NULL,
    `padNumber` int,
    `latitude` decimal(10, 8),
    `longitude` decimal(11, 8),
    `notes` text,
    PRIMARY KEY (`id`)
);

CREATE TABLE `recyclingCentres` (
    `id` varchar(40) DEFAULT (uuid()),
    `createdAt` timestamp DEFAULT current_timestamp NOT NULL,
    `updatedAt` timestamp DEFAULT current_timestamp ON UPDATE current_timestamp NOT NULL,
    `location` varchar(80) NOT NULL,
    `parish` varchar(30) NOT NULL,
    `latitude` decimal(10, 8),
    `longitude` decimal(11, 8),
    `notes` text,
    PRIMARY KEY (`id`)
);

CREATE TABLE `recyclingCentreServices` (
    `id` int auto_increment NOT NULL,
    `recyclingCentreId` varchar(40) NOT NULL,
    `service` varchar(50) NOT NULL, 
    FOREIGN KEY (`recyclingCentreId`) REFERENCES `recyclingCentres`(`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
);