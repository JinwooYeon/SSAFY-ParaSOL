-- MySQL dump 10.13  Distrib 8.0.27, for Win64 (x86_64)
--
-- Host: k6s1011.p.ssafy.io    Database: corebank
-- ------------------------------------------------------
-- Server version	5.7.38

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `transaction_history`
--

DROP TABLE IF EXISTS `transaction_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` bigint(20) DEFAULT NULL,
  `date` bigint(20) DEFAULT NULL,
  `transaction_account` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transaction_opponent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `account` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `account_number_opponent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name_opponent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKavsdj6kmq0ps97trc14nsac02` (`account`),
  CONSTRAINT `FKavsdj6kmq0ps97trc14nsac02` FOREIGN KEY (`account`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=226767 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_history`
--

LOCK TABLES `transaction_history` WRITE;
/*!40000 ALTER TABLE `transaction_history` DISABLE KEYS */;
INSERT INTO `transaction_history` VALUES (2,80000,1652939974736,NULL,NULL,1,'777-777-777777',NULL,'ParaSOL Pay'),(3,80000,1652943352990,NULL,NULL,1,'999-999-999999',NULL,'ParaSOL Pay'),(4,20000,1652944607219,NULL,NULL,1,'999-999-999999',NULL,'ParaSOL Pay'),(5,100,1652947859207,NULL,NULL,0,'777-777-777777',NULL,'100'),(6,1000000,1652948063039,NULL,NULL,0,'999-999-999999',NULL,'test'),(7,10000,1652948309006,NULL,NULL,0,'999-999-999999',NULL,'ParaSOL pay'),(8,90000,1652948322453,NULL,NULL,1,'999-999-999999',NULL,'ParaSOL Pay'),(9,90000,1652948326014,NULL,NULL,0,'999-999-999999',NULL,'ParaSOL pay'),(10,30000,1652949645795,NULL,NULL,1,'999-999-999999',NULL,'ParaSOL Pay'),(11,30000,1652949649031,NULL,NULL,0,'999-999-999999',NULL,'ParaSOL pay'),(118966,80000,1652966380815,NULL,NULL,1,'777-777-777777',NULL,'ParaSOL Pay'),(118967,50000,1652966388914,NULL,NULL,0,'777-777-777777',NULL,'ParaSOL pay'),(226760,80000,1652969916015,NULL,NULL,1,'777-777-777777',NULL,'ParaSOL Pay'),(226761,40000,1652969928470,NULL,NULL,0,'777-777-777777',NULL,'ParaSOL pay'),(226762,30000,1652971472948,NULL,NULL,1,'777-777-777777',NULL,'ParaSOL Pay'),(226763,30000,1652972039787,NULL,NULL,1,'777-777-777777',NULL,'ParaSOL Pay'),(226764,10000,1652974569546,NULL,NULL,0,'123-123-123456',NULL,'?????????'),(226765,1000,1652977138543,NULL,NULL,0,'777-777-777777',NULL,'aa'),(226766,1000,1652977414378,NULL,NULL,1,'777-777-777777',NULL,'ss');
/*!40000 ALTER TABLE `transaction_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-05-20  2:26:39
