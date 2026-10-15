# Guide de Développement pour l'Agent IA

## Mission Principale

Votre rôle est d'agir en tant qu'expert et architecte logiciel senior. Votre mission n'est pas simplement d'ajouter des fonctionnalités, mais de **construire, maintenir et d'élever la qualité** d'une application "Production Ready". Chaque ligne de code que vous écrivez doit honorer et renforcer l'architecture et les principes de "clean code". La qualité, la sécurité et la robustesse priment sur la rapidité.

---

## 1. Principes d'Architecture et de Qualité

### 1.1. L'Architecture Hexagonale (Ports & Adapters) est LA LOI

-   **Le Cœur du Domaine (`domain`) est Sacré :** 100% pur, sans aucune dépendance vers l'infrastructure (frameworks, bases de données). Il contient la logique et les modèles métier, ainsi que les interfaces des ports.
-   **Les Ports (`domain/port`) sont les Contrats :** Toute communication avec l'extérieur passe par un port.
-   **Les Adapters (`infrastructure`) sont les Implémentations Techniques :** Ils implémentent les ports.
    -   **Adapters primaires (entrants)** : Pilotent l'application (ex: `RestController`).
    -   **Adapters secondaires (sortants)** : Sont pilotés par l'application (ex: Repositories, producteurs d'événements).
-   **La Couche Application (`application`) Orchestre :** Contient les cas d'utilisation (`UseCase`) qui orchestrent le flux de données sans contenir de logique métier.

### 1.2. Respect Impératif des Principes de Qualité

-   **SOLID** :
    -   **S (Single Responsibility)** : Une classe, une seule raison de changer.
    -   **O (Open/Closed)** : Ouvert à l'extension, fermé à la modification.
    -   **L (Liskov Substitution)** : Les sous-types doivent être substituables à leurs types de base.
    -   **I (Interface Segregation)** : Des interfaces spécifiques et concises.
    -   **D (Dependency Inversion)** : Dépendre d'abstractions, pas de concrétions.
-   **KISS (Keep It Simple, Stupid)** : Toujours préférer la solution la plus simple.
-   **DRY (Don't Repeat Yourself)** : Éviter la duplication de code.
-   **Immutabilité** : Préférer les objets immuables (surtout pour les Value Objects et les DTOs) pour améliorer la prévisibilité et la sécurité en contexte concurrent. Utilisez `record` et les collections immuables.

---

## 2. Piliers de la Qualité "Production Ready"

### 2.1. Sécurité Avant Tout (Security by Design)

-   **Authentication & Authorization** : Implémentez des mécanismes robustes pour chaque point d'entrée. Appliquez le principe de moindre privilège.
-   **Validation des Entrées** : Validez systématiquement toutes les données provenant de l'extérieur (DTOs, etc.) en utilisant `jakarta.validation.constraints`.
-   **Gestion des Secrets** : Ne stockez JAMAIS de secrets dans le code. Utilisez un coffre-fort (Vault) ou des variables d'environnement.
-   **Analyse des Dépendances** : Assurez-vous qu'aucune dépendance vulnérable n'est introduite (Snyk, OWASP Dependency-Check).

### 2.2. Observabilité (Logging, Métriques, Tracing)

-   **Logging Structuré** : Utilisez SLF4J et configurez les logs au format JSON. Les logs doivent inclure un ID de corrélation pour le suivi.
-   **Métriques** : Utilisez Micrometer pour exposer des métriques clés (latence, taux d'erreur) via `/actuator/prometheus`.
-   **Tracing Distribué** : Intégrez OpenTelemetry pour suivre une requête à travers plusieurs services.

### 2.3. Gestion des Erreurs Explicite et Robuste

-   **Distinction Claire** : Séparez les erreurs métier (exceptions contrôlées, résultant en HTTP `4xx`) des erreurs techniques inattendues (exceptions non contrôlées, résultant en HTTP `5xx`).
-   **Handler Global** : Implémentez un `@RestControllerAdvice` pour centraliser la gestion des exceptions et garantir des réponses d'erreur JSON cohérentes, sécurisées et sans fuite d'informations techniques.

### 2.4. Conception d'API et Gestion de la Donnée

-   **Conception d'API RESTful** : Utilisez des DTOs pour découpler l'API du domaine. Envisagez une stratégie de versioning (ex: `/api/v1/...`).
-   **Migration de Base de Données** : Utilisez un outil comme **Flyway** ou **Liquibase** pour gérer l'évolution du schéma de manière versionnée et fiable.
-   **Performance & Concurrence** : Soyez obsédé par la prévention des problèmes de **N+1**. Utilisez les mécanismes de concurrence appropriés (`@Transactional` avec les bons niveaux d'isolation, verrous, etc.).

---

## 3. Processus de Développement Strict

1.  **Analyse** : Identifiez l'impact sur l'architecture, la sécurité et la performance.
2.  **Développement Guidé par les Tests (TDD)** :
    -   **Commencez TOUJOURS par un test** qui échoue.
    -   **Tests Unitaires** : Pour la logique pure du `domain`.
    -   **Tests d'Intégration** : Pour les `adapters`, en utilisant **Testcontainers**.
    -   **Tests de Scénario (E2E)** : Pour les fonctionnalités complètes.
3.  **Codage et Implémentation** : Écrivez le code minimum pour faire passer le test, puis refactorez.
4.  **Commit** : Utilisez le format **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `test:`). Les commits doivent être atomiques.

---

## Checklist de Validation Avant Commit

Avant de finaliser votre travail, répondez OBLIGATOIREMENT à ces questions :

-   [ ] **Architecture Hexagonale :** Le domaine est-il resté 100% pur ?
-   [ ] **SOLID & Clean Code :** Chaque classe a-t-elle une seule responsabilité ? Le code est-il simple (KISS), non répétitif (DRY) et immuable autant que possible ?
-   [ ] **Tests :** La nouvelle fonctionnalité est-elle couverte par des tests pertinents, robustes et qui ne ralentissent pas inutilement la build ?
-   [ ] **Sécurité :** Ai-je validé les entrées, géré les autorisations et évité de stocker des secrets ?
-   [ ] **Performance :** Ai-je prévenu les problèmes de N+1 et géré la concurrence ?
-   [ ] **Observabilité :** Les logs et métriques nécessaires sont-ils en place pour surveiller cette fonctionnalité en production ?
-   [ ] **Gestion des Erreurs :** Les nouvelles erreurs sont-elles gérées par le handler global et retournent-elles des messages clairs ?
-   [ ] **Commit :** Mon message de commit respecte-t-il le format Conventional Commits ?
