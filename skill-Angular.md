# Guide de Développement Angular - Agent Expert Senior

Ce document rassemble l'ensemble des règles de codage, de sécurité, d'optimisation, d'accessibilité, de tests et de gestion de version (Git) que l'agent d'assistance au code doit impérativement respecter pour produire un code Angular robuste, performant, sécurisé, accessible et maintenable de niveau développeur senior.

---

## Sommaire
1. [Architecture & Organisation du Projet](#1-architecture--organisation-du-projet)
2. [Style de Codage & Conventions TypeScript](#2-style-de-codage--conventions-typescript)
3. [Gestion de l'État & Réactivité (Signals & RxJS)](#3-gestion-de-létat--réactivité-signals--rxjs)
4. [Optimisation du Rendu & Templates](#4-optimisation-du-rendu--templates)
5. [Performance & Chargement (Bundling, SSR & Hydratation)](#5-performance--chargement-bundling-ssr--hydratation)
6. [Sécurité & Configuration](#6-sécurité--configuration)
7. [Stratégie de Test (Jest & Playwright)](#7-stratégie-de-test-jest--playwright)
8. [Accessibilité (a11y)](#8-accessibilité-a11y)
9. [Conventions Git & Workflow de Commit](#9-conventions-git--workflow-de-commit)

---

## 1. Architecture & Organisation du Projet

### 1.1. Structure Globale du Projet
L'architecture de l'application doit être modulaire et découplée, structurée de la manière suivante :

```
src/
├── app/                      # Code source de l'application
│   ├── core/                 # Services globaux singletons, guards, intercepteurs, configurations
│   │   ├── guards/
│   │   ├── interceptors/
│   │   └── services/
│   ├── features/             # Fonctionnalités/modules métiers (découpés par domaine)
│   │   ├── feature-a/
│   │   │   ├── components/
│   │   │   ├── services/
│   │   │   └── feature-a.routes.ts
│   │   └── feature-b/
│   └── shared/               # Composants, directives, et pipes réutilisables, purement présentationnels
│       ├── components/
│       ├── directives/
│       └── pipes/
├── assets/                   # Fichiers statiques (images, polices)
└── environments/             # Fichiers de configuration par environnement
e2e/                          # Tests End-to-End avec Playwright
```

### 1.2. Standalone by Default
- **Tous** les composants, directives et pipes doivent être déclarés en tant que `standalone: true`. Les `NgModule` classiques sont interdits, sauf intégration incontournable avec des librairies tierces obsolètes.
- Utilisez la configuration des routes standalone via `provideRouter` pour charger dynamiquement les modules fonctionnels (Lazy Loading).

### 1.3. Pattern Smart/Dumb Components
Séparez rigoureusement les responsabilités des composants pour optimiser la réactivité et la réutilisation :
- **Smart Components (Containers) :** Gèrent la logique métier, injectent les services, s'abonnent aux flux de données (Signals ou RxJS), et orchestrent les flux. Ils résident principalement dans `features/`.
- **Dumb Components (Présentationnels) :** Reçoivent leurs données via `@Input` (de préférence sous forme de `input()` signal-based) et communiquent avec l'extérieur via des `@Output` (de préférence sous forme de `output()`). Ils ne contiennent aucune logique métier et n'injectent aucun service de données. Ils résident dans `shared/` ou dans le sous-dossier `components/` d'une feature.

### 1.4. Injection de Dépendances (DI) moderne
- Utilisez exclusivement la fonction `inject()` pour l'injection de dépendances, que ce soit pour les variables de classe ou dans les initialiseurs de propriétés.
- Déclarez les dépendances injectées en `readonly` pour garantir l'immutabilité de l'architecture.
- **Exemple :**
  ```typescript
  @Component({ ... })
  export class MyComponent {
    private readonly dataService = inject(DataService);
  }
  ```

### 1.5. Intercepteurs et Gardes Fonctionnels (Functional Guards & Interceptors)
- Bannissez l'usage de classes implémentant les anciennes interfaces `CanActivate` ou `HttpInterceptor`.
- Utilisez impérativement les guards fonctionnels (`CanActivateFn`) et les intercepteurs fonctionnels (`HttpInterceptorFn`) pour plus de simplicité, une meilleure intégration avec l'injection de dépendances moderne via `inject()`, et un tree-shaking optimal lors de la compilation.
- **Exemple de Guard Fonctionnel :**
  ```typescript
  export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    return authService.isAuthenticated() ? true : router.createUrlTree(['/login']);
  };
  ```
- **Exemple d'Intercepteur Fonctionnel :**
  ```typescript
  export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = authService.getToken();
    if (token) {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
      return next(cloned);
    }
    return next(req);
  };
  ```

---

## 2. Style de Codage & Conventions TypeScript

### 2.1. Règles TypeScript Strictes
- **Target :** `ES2022`. Le mode strict dans `tsconfig.json` est **obligatoire**.
- **Indentation :** 2 espaces.
- **Convention de nommage :** `camelCase` pour les fonctions et variables, `PascalCase` pour les classes (Composants, Services, Directives, Pipes), `UPPERCASE` pour les constantes globales.
- **Exports :** Favorisez toujours les `named exports` par défaut au détriment des `default exports`.
- **Interdiction du type `any` :** L'usage de `any` est proscrit. Utilisez des types stricts, des interfaces précises ou `unknown` si le type est dynamique.
- **Imports de type :** Utilisez explicitement `import type { ... }` lors de l'importation de types ou d'interfaces uniquement, afin d'optimiser l'étape de compilation et le tree-shaking.
- **Équivalence stricte :** Toujours utiliser `===` et `!==` au lieu de `==` et `!=`.
- **Déclarations :** Utilisez `const` par défaut, et `let` uniquement si une réassignation est absolument nécessaire. Le mot-clé `var` est strictly interdit.
- **Opérateurs modernes :** Utilisez systématiquement le "nullish coalescing" (`??`) et l' "optional chaining" (`?.`).
- **Assertion non-nulle (`!`) interdite :** Gérez les valeurs potentiellement `null` ou `undefined` de manière explicite (guards de type, valeurs par défaut).
- **Early Returns :** Privilégiez les retours anticipés (early returns) pour réduire la complexité cognitive et éviter les imbrications de blocs `if / else`.
- **Variables non utilisées :** Préfixez par un underscore (`_`) toute variable d'argument requise mais non utilisée.
- **Alias de chemin :** Utilisez toujours `@/*` (ex: `@/core`, `@/shared`, `@/features`) configuré dans `tsconfig.json` pour éviter des chemins relatifs longs et illisibles.

### 2.2. Formatage (Prettier) & Linting (ESLint)
- Le formateur standard est **Prettier** (`.prettierrc.json`). Lancer `pnpm format` pour formater ou `pnpm format:check` pour vérifier.
- Le linter standard est **ESLint** configuré avec `@angular-eslint` (`.eslintrc.json`). Lancer `pnpm lint` ou `pnpm lint:fix`.
- **Limites strictes de complexité et de taille (règles de linter) :**
  - Complexité cyclomatique : max **8**
  - Nombre de lignes par fonction : max **50**
  - Nombre de paramètres par fonction : max **4**
  - Profondeur d'imbrication (max-depth) : max **3**
  - Callbacks imbriquées (max-nested-callbacks) : max **2**
  - Instructions par fonction (max-statements) : max **10**
  - Règle `@typescript-eslint/no-explicit-any` : configurée sur `error`.
  - Règle `eqeqeq` : configurée sur `always`.

### 2.3. Style CSS / SCSS
- **Styles encapsulés (Scoped) :** Les styles doivent être encapsulés au niveau de leur composant (comportement par défaut) et rédigés dans un fichier `.scss` co-localisé.
- **Tokens du Design System :** N'utilisez aucune valeur magique (couleurs en dur, pixels pour les marges). Utilisez exclusivement les variables CSS globales ou de thème fournies par le design system (variables `--ds-*`).
- **Convention de nommage CSS :** Utilisez la convention BEM (`block__element--modifier`) même avec l'encapsulation, afin d'assurer la lisibilité et l'organisation sémantique du template HTML.
- **Bibliothèque de composants :** Utilisez **Angular Material** (`@angular/material` + `@angular/cdk`) comme design system de composants. (PrimeNG a été écarté : depuis la v22, PrimeTek l'a fermé en commercial — licence requise, sinon bandeau d'avertissement affiché en production.)

### 2.4. Ordre des Imports
Respectez l'ordre suivant dans les fichiers TypeScript pour faciliter la relecture :
1. Dépendances externes de base (`@angular/*`, `rxjs`, `rxjs/operators`).
2. Librairies tierces installées (`@ng-bootstrap`, etc.).
3. Alias de chemin internes configurés dans `tsconfig.json` (`@/core/*`, `@/shared/*`, `@/features/*`).
4. Imports relatifs (`./...`, `../...`).

---

## 3. Gestion de l'État & Réactivité (Signals & RxJS)

Angular moderne s'appuie sur une synergie entre les **Signals** (réactivité synchrone et état de vue) et **RxJS** (flux asynchrones, événements complexes, requêtes HTTP).

### 3.1. Approche "Signals First"
- **État local du composant :** Tout état modifiable localement au sein d'un composant doit être géré avec des `Signal` (`signal()`).
- **Propriétés calculées :** Utilisez impérativement `computed()` pour toutes les valeurs dérivées. Les signaux calculés sont mémorisés (cached) et ne se ré-évaluent que lorsque leurs signaux dépendants changent, éliminant les recalculs inutiles dans les templates HTML.
- **Lecture asynchrone des entrées :** Privilégiez les inputs sous forme de signaux (`input()`, `input.required()`) et les modèles bidirectionnels (`model()`) pour suivre naturellement les changements d'états d'entrées.
- **Modifications d'états liées :** Utilisez `effect()` uniquement pour des effets de bord indispensables (comme des manipulations directes du DOM hors Angular, la synchronisation avec le `localStorage`, ou des appels à des API d'analyse/analytics). N'utilisez jamais un `effect()` pour modifier un autre signal (risque de boucles infinies ou d'incohérences d'état).

### 3.2. État Partagé et Store de Service (Pattern Unidirectionnel)
- Pour la gestion d'un état global ou partagé entre plusieurs composants, implémentez un service store injectable au niveau `core` ou `feature`.
- L'état modifiable interne doit être déclaré via un signal d'écriture privé (`private readonly _state = signal(...)`).
- Exposez l'état publiquement uniquement sous forme de signal en lecture seule (`public readonly state = this._state.asReadonly()`).
- Toute modification d'état doit s'effectuer de manière contrôlée et explicite via des méthodes de modification publiques (actions) définies dans le service.
- **Exemple :**
  ```typescript
  @Injectable({ providedIn: 'root' })
  export class CartStore {
    private readonly _items = signal<CartItem[]>([]);
    
    // Exposition en lecture seule
    public readonly items = this._items.asReadonly();
    public readonly totalCount = computed(() => this._items().reduce((acc, item) => acc + item.quantity, 0));
    
    public addItem(item: CartItem): void {
      this._items.update(currentItems => {
        const existingIndex = currentItems.findIndex(i => i.id === item.id);
        if (existingIndex > -1) {
          const updated = [...currentItems];
          updated[existingIndex] = { ...updated[existingIndex], quantity: updated[existingIndex].quantity + item.quantity };
          return updated;
        }
        return [...currentItems, item];
      });
    }
    
    public clearCart(): void {
      this._items.set([]);
    }
  }
  ```

### 3.3. RxJS pour les Événements Complexes et Requêtes HTTP
- Conservez RxJS pour la gestion d'événements asynchrones complexes (ex: saisies clavier avec debounce, drag and drop, polling, WebSocket).
- Pour l'intégration entre les deux mondes, utilisez les utilitaires d'interopérabilité officiels d'Angular :
  - `toSignal()` : Pour transformer un Observable en Signal (très utile pour convertir les requêtes HTTP ou les paramètres de route en signaux de template).
  - `toObservable()` : Pour écouter les changements d'un Signal et déclencher des traitements RxJS asynchrones.

### 3.4. Gestion Critique des Souscriptions RxJS
Pour éviter les fuites de mémoire (memory leaks) qui saturent le navigateur, appliquez strictement :
- **Le pipe `async` :** C'est la méthode de choix pour souscrire à un Observable directement dans le template HTML. Il gère automatiquement l'inscription et la désinscription lors du cycle de vie du composant.
- **L'opérateur `takeUntilDestroyed()` :** À utiliser si vous devez souscrire manuellement dans la partie TypeScript (composant ou service). Sauf indication de contexte (`DestroyRef` explicite), `takeUntilDestroyed()` doit être placé dans le contexte de création (généralement le constructeur).
- **Pas de souscriptions infinies :** Évitez de laisser des souscriptions manuelles actives sans opérateur de complétion.

### 3.5. Opérateurs RxJS d'Optimisation de Requêtes
- **`debounceTime` et `distinctUntilChanged` :** À coupler systématiquement sur les champs de recherche ou les filtres de saisie pour éviter d'envoyer des requêtes inutiles au serveur.
- **`shareReplay(1)` :** Utilisez cet opérateur pour mettre en cache et partager le résultat d'un appel HTTP lourd entre plusieurs souscripteurs.
- **`switchMap` vs `mergeMap` / `concatMap` / `exhaustMap` :**
  - Utilisez `switchMap` pour les recherches ou changements de filtres (annule la requête précédente obsolète).
  - Utilisez `concatMap` si l'ordre séquentiel strict d'exécution est requis.
  - Utilisez `exhaustMap` pour les soumissions de formulaires afin d'ignorer les clics répétés (double-clics accidentels).

---

## 4. Optimisation du Rendu & Templates

### 4.1. Stratégie Change Detection `OnPush`
- **Obligatoire sur TOUS les composants :**
  ```typescript
  @Component({
    ...
    changeDetection: ChangeDetectionStrategy.OnPush
  })
  ```
- Avec `OnPush`, Angular ne vérifie le composant que si une référence d'`@Input()` change, si un événement est émis depuis le composant lui-même, ou si un Signal lu dans le template change de valeur. Cela réduit drastiquement l'arbre de rendu évalué à chaque cycle de détection.

### 4.2. Nouveau Control Flow d'Angular (`@`)
- Utilisez exclusivement le nouveau flux de contrôle natif introduit en Angular 17+ (`@if`, `@for`, `@switch`) à la place des anciennes directives structurelles (`*ngIf`, `*ngFor`, `*ngSwitch`).
- **`track` est obligatoire dans `@for` :** Vous devez toujours fournir une expression `track` unique (par exemple, un ID unique ou l'élément lui-même s'il est immuable) pour permettre à Angular d'identifier les éléments DOM et d'éviter de reconstruire la liste complète lors d'une mise à jour.
- **Exemple :**
  ```html
  @for (item of items(); track item.id) {
    <div>{{ item.name }}</div>
  } @empty {
    <p>Aucun élément disponible</p>
  }
  ```

### 4.3. Interdiction absolue des Appels de Fonctions dans les Templates
- N'appelez jamais de fonction ordinaire directement dans les expressions de liaison de votre template HTML (ex: `<p>{{ getFormattedName(user) }}</p>` ou `[disabled]="isFormInvalid()"` avec une fonction pure TS).
- **Pourquoi ?** Ces fonctions sont ré-exécutées à absolument chaque micro-cycle de détection des changements (parfois des dizaines de fois par seconde), détruisant les performances de rendu.
- **Solutions autorisées :**
  - Utilisez un **Signal calculé** (`computed()`).
  - Utilisez un **Pipe pur** (`@Pipe({ name: 'myPipe', pure: true })`). Les pipes purs mettent en cache leur dernier résultat et ne se ré-évaluent que si leurs arguments changent (par référence).

---

## 5. Performance & Chargement (Bundling, SSR & Hydratation)

### 5.1. Deferrable Views (`@defer`)
- Utilisez le bloc `@defer` pour retarder le chargement et le rendu des composants lourds ou secondaires (graphiques complexes, éditeurs de texte riches, modales, commentaires en bas de page).
- **Pratique recommandée :** Définir des déclencheurs intelligents (`on viewport` pour charger quand l'élément devient visible, `on idle` pour charger pendant les temps morts du navigateur, ou `on interaction`).
- **Exemple :**
  ```html
  @defer (on viewport) {
    <app-heavy-chart [data]="chartData()" />
  } @placeholder {
    <div class="skeleton">Chargement du graphique...</div>
  } @loading (minimum 500ms) {
    <app-spinner />
  }
  ```

### 5.2. Lazy Loading des Routes
- Découpez l'application en modules ou groupes de composants indépendants.
- Chargez les routes fonctionnelles via des imports dynamiques au niveau de la configuration des routes principales :
  ```typescript
  export const APP_ROUTES: Route[] = [
    {
      path: 'dashboard',
      loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
    },
    {
      path: 'admin',
      loadChildren: () => import('./features/admin/admin.routes').then(r => r.ADMIN_ROUTES)
    }
  ];
  ```

### 5.3. Server-Side Rendering (SSR) & Hydratation
- Configurez le SSR natif d'Angular pour optimiser l'indexation SEO et le First Contentful Paint (FCP).
- Activez l'**Hydratation Stable (Client-Side Hydration)** pour éviter les clignotements d'écran et la reconstruction complète du DOM par le client après le chargement du HTML pré-généré par le serveur.
- Évitez les manipulations directes du DOM global (`window`, `document`, `navigator`) dans le code commun. Si nécessaire, encapsulez ces appels dans des vérifications de plateforme (`isPlatformBrowser(platformId)`) ou utilisez les abstractions Angular (`Renderer2`, `DOCUMENT`).

### 5.4. Optimisation des Images (`NgOptimizedImage`)
- Pour toutes les images critiques, utilisez impérativement la directive native `NgOptimizedImage` (via l'attribut `ngSrc` au lieu de `src`).
- Cette directive applique automatiquement le chargement paresseux (lazy loading), génère les attributs de dimensionnement nécessaires pour prévenir les décalages de mise en page (Cumulative Layout Shift - CLS), et ajuste la priorité des images importantes situées au-dessus du pli (LCP - Largest Contentful Paint) via l'attribut `priority`.

### 5.5. Web Workers pour les Calculs Intensifs
- Si l'application requiert des calculs mathématiques lourds, du traitement d'images ou la manipulation d'importants volumes de données, déléguez ces opérations dans un **Web Worker** pour exécuter le code en tâche de fond et préserver la fluidité de la boucle d'interaction principale (60 FPS).

### 5.6. Analyse et Profiling du Bundle
- Utilisez régulièrement `source-map-explorer` ou `webpack-bundle-analyzer` pour inspecter la taille des paquets générés.
- Supprimez les librairies lourdes non tree-shakables (comme `Moment.js`) au profit de solutions légères natives (comme l'API native `Intl` du navigateur) ou de petites bibliothèques modulaires (comme `date-fns`).
- Utilisez l'extension officielle **Angular DevTools** pour analyser l'activité du change detection et profiler les performances de rendu des composants en temps réel.

---

## 6. Sécurité & Configuration

### 6.1. Gestion Strictement Sécurisée des Environnements
- **Zéro Secret dans le Repository :** Aucun secret, clé privée d'API, mot de passe ou token ne doit être écrit en dur dans le code source ni commité dans le dépôt Git (exclure formellement les fichiers contenant des secrets de `.git`).
- Utilisez les variables d'environnement configurées au niveau de la chaîne d'intégration/déploiement (CI/CD) et injectées lors de l'étape de compilation (`pnpm build`).
- **Gestionnaire de paquets :** Utilisez impérativement `pnpm` (version `>= 10.28.2`).
- **Node.js :** Exigez une version de Node supérieure ou égale à `22`.

### 6.2. Prévention des Failles XSS (Cross-Site Scripting)
- Angular assainit nativement toutes les valeurs interpolées (`{{ value }}`) et liées via le property binding (`[property]="value"`).
- **Interdiction de contourner la sécurité :** N'utilisez jamais les méthodes de contournement comme `bypassSecurityTrustHtml()`, `bypassSecurityTrustScript()` ou `bypassSecurityTrustStyle()` sauf cas d'extrême nécessité validé et documenté.
- **Rendu de HTML dynamique :** Si vous devez absolument injecter du code HTML dynamique externe, utilisez le `DomSanitizer` d'Angular, mais passez obligatoirement le contenu brut au préalable dans une bibliothèque d'assainissement robuste côté client comme **DOMPurify** :
  ```typescript
  import DOMPurify from 'dompurify';
  
  export class MyComponent {
    private readonly sanitizer = inject(DomSanitizer);
    
    public getSafeHtml(unsafeHtml: string): SafeHtml {
      const cleanHtml = DOMPurify.sanitize(unsafeHtml);
      return this.sanitizer.bypassSecurityTrustHtml(cleanHtml);
    }
  }
  ```

### 6.3. Sécurisation des Données et des Flux
- **Validation stricte des API :** Validez et nettoyez systématiquement toutes les structures de données reçues des services et API externes avant de les propager dans l'application ou l'interface de l'utilisateur.
- **Pas de données sensibles dans les URLs :** Ne faites jamais transiter de jetons d'accès, mots de passe ou informations d'identification dans les paramètres de requête URL (`queryParameters`). Utilisez les en-têtes HTTP de sécurité ou, à défaut, le corps d'une requête POST.
- **Pas de Logs de Production :** Assurez-vous qu'aucun `console.log` ne divulgue d'informations utilisateur sensibles ou de jetons d'authentification. Configurez le linter pour interdire les `console.log` non autorisés en production (ou les désactiver via des plugins dédiés en build).
- **Intercepteurs HTTP de Sécurité :** Centralisez l'ajout de vos en-têtes d'autorisation (ex: jeton Bearer JWT) et la gestion des codes de retour liés à la sécurité (ex: redirection automatique vers la page de connexion sur une erreur 401 ou 403) au sein d'intercepteurs fonctionnels (`HttpInterceptorFn`) configurés globalement.
- **Désactivation des règles de sécurité interdite :** Ne désactivez jamais les règles de linter liées à la sécurité (comme via des commentaires `eslint-disable`) sans une justification documentée et rigoureusement validée par l'équipe.

### 6.4. Maintenance des Dépendances
- **Versions épinglées :** Fixez de manière stricte les versions des dépendances clés dans le fichier `package.json` afin de bloquer les mises à jour mineures non validées qui pourraient introduire des failles ou régressions.
- **Audit de sécurité :** Exécutez régulièrement la commande `pnpm audit` pour identifier et corriger immédiatement les vulnérabilités détectées au sein des paquets tiers.

---

## 7. Stratégie de Test (Jest & Playwright)

Une fonctionnalité n'est considérée comme achevée que lorsqu'elle dispose d'une suite de tests complète validant son bon fonctionnement et excluant tout risque de régression.

### 7.1. Tests Unitaires & d'Intégration (Jest)
- **Framework de test :** **Jest** (`jest.config.js` à la racine).
- **Environnement de simulation :** `jsdom`.
- **Fichiers de test :** Co-localisés avec les composants et services sous l'extension `*.spec.ts`.
- **Objectif de Couverture :** Un minimum de **90%** de couverture de code pour les lignes, fonctions, et branches est requis.
- **Commandes d'exécution :**
  - Lanceur en tâche de fond (Watch Mode) : `pnpm test`
  - Rapport complet de couverture : `pnpm test:coverage` (utilise le provider natif `v8`).

### 7.2. Bonnes Pratiques pour Jest
- **Tests Unitaires Purs (Logique métier, Services, Pipes) :**
  - **Ne pas utiliser le `TestBed` d'Angular.** Instanciez simplement les classes de vos services ou pipes manuellement en passant des versions mockées simples de leurs dépendances. Cela garantit une vitesse d'exécution optimale et une isolation parfaite.
  - **Exemple :**
    ```typescript
    const mockHttpClient = {} as HttpClient;
    const service = new MyService(mockHttpClient);
    ```
- **Tests d'Intégration (Composants) :**
  - Utilisez le `TestBed` pour configurer le composant à tester de manière minimale.
  - **Isolation stricte :** Moquez obligatoirement toutes les dépendances lourdes (services d'API, services globaux) et configurez les composants enfants ou dépendances de rendu secondaires à l'aide de mocks légers.
  - **Interactions de type Utilisateur :** Ne testez pas l'implémentation interne en appelant directement des méthodes privées de votre TypeScript. Testez le comportement extérieur en simulant de réelles actions utilisateur sur le DOM (clics sur des boutons via `triggerEventHandler`, saisies de texte).
  - **Angular CDK Harnesses :** Utilisez de préférence les `Harnesses` de `@angular/cdk/testing` pour interagir avec les composants graphiques de manière robuste, ce qui évite de casser les tests si la structure interne du DOM du composant est modifiée.

### 7.3. Tests End-to-End (Playwright)
- **Framework :** **Playwright** (`playwright.config.ts`).
- **Localisation des fichiers :** Dossier `/e2e` à la racine du projet.
- **URL de Base :** `http://localhost:4200` (port de développement par défaut d'Angular).
- **Navigateurs de validation :** Chromium, Firefox, WebKit (Safari).
- **Cycle d'exécution :** Playwright démarre et éteint automatiquement le serveur de développement local (`ng serve`) à chaque passage de test complet.
- **Commandes d'exécution :**
  - Lancement des tests E2E : `pnpm test:e2e`
  - Lancement interactif avec interface utilisateur (UI Mode) : `pnpm test:e2e --ui`

---

## 8. Accessibilité (a11y)

Le respect de l'accessibilité (normes WCAG AA) est un critère de qualité obligatoire pour toutes les interfaces utilisateur produites dans ce projet.

### 8.1. Linting & Validation HTML d'Accessibilité
- Le linter intègre le plugin de validation de template `@angular-eslint/template/accessibility` pour bloquer les templates non conformes directement au build/lint.
- **Règles clés de template appliquées :**
  - `accessibility-alt-text` : Tout élément `<img>` doit posséder un attribut `alt` (vide `alt=""` pour les éléments purement décoratifs).
  - `accessibility-elements-content` : Les éléments interactifs ou de titre doivent posséder un contenu sémantique lisible par un lecteur d'écran.
  - `accessibility-label-for` : Les champs de formulaires doivent obligatoirement être liés à une étiquette `<label>` via l'attribut `for` ou posséder un attribut `aria-label` / `aria-labelledby`.
  - `accessibility-role-has-required-aria` : Les éléments utilisant un rôle ARIA spécifique doivent implémenter tous les attributs d'état ARIA obligatoires associés à ce rôle.
  - `click-events-have-key-events` et `mouse-events-have-key-events` : Tout événement d'interaction souris (`(click)`, `(mouseover)`) sur un élément non natif interactif doit être accompagné de son équivalent clavier (`(keydown)`, etc.) pour assurer la navigation complète sans souris.

### 8.2. Principes Fondamentaux de Conception Accessible
- **HTML Sémantique :** Utilisez impérativement les éléments natifs de HTML5 (`<button>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<header>`, `<footer>`) plutôt que des surcharges de `<div>` ou `<span>`. Ne redéfinissez jamais de rôle natif (ex: pas de `role="button"` sur un lien `<a>`).
- **Contraste de Couleur :** Respectez les exigences de contraste minimum (WCAG AA) : **4.5:1** pour le texte normal et **3:1** pour le grand texte. Ne transmettez jamais une information essentielle par la couleur seule (accompagnez-la d'un texte, d'un motif ou d'une icône explicite).
- **Navigation au Clavier :**
  - Assurez un indicateur visuel de focus clair et visible (`:focus` ou `:focus-visible`). Ne masquez jamais le contour de focus par CSS (`outline: none`) sans fournir d'alternative contrastée.
  - L'ordre de tabulation (`tabindex`) doit suivre l'ordre de lecture et de structure logique du DOM.
- **Gestion des Erreurs :** Liez sémantiquement les messages d'erreur de formulaire aux champs correspondants via l'attribut `aria-describedby` pour que les lecteurs d'écran les annoncent lors de la prise de focus.
- **Contenus Dynamiques :** Utilisez `aria-live="polite"` pour annoncer les alertes non intrusives, notifications ou mises à jour asynchrones à l'utilisateur sans perturber son focus en cours.

### 8.3. Gestion Avancée du Focus avec Angular CDK
Pour les composants d'interface dynamiques comme les modales, volets latéraux (drawers), pop-ups ou dropdowns, utilisez le module **`A11yModule`** d'Angular CDK :
- **Piégeage de focus (Focus Trap) :** Utilisez la directive `cdkTrapFocus` pour retenir le focus à l'intérieur du composant ouvert (l'empêcher de s'échapper vers les éléments arrière-plans avec `Tab`).
- **Retour du focus :** À la fermeture du composant dynamique, le focus doit automatiquement être restitué à l'élément déclencheur initial.
- **FocusMonitor :** Suivez la provenance des focus (clavier, souris, programmatique) via le service `FocusMonitor` pour adapter le style visuel de manière optimale.
- **LiveAnnouncer :** Utilisez le service `LiveAnnouncer` pour émettre des messages sonores explicites et dynamiques à destination des lecteurs d'écran.
- **Tests d'Accessibilité :** Intégrez **`axe-core`** dans vos tests de bout en bout (`@axe-core/playwright`) pour exécuter des audits automatiques réguliers en CI/CD.

---

## 9. Conventions Git & Workflow de Commit

Pour maintenir un historique propre, compréhensible et automatisable, respectez rigoureusement les conventions Git suivantes.

### 9.1. Conventional Commits (commitlint)
Tous les messages de validation de commit doivent suivre la spécification Conventional Commits, validée de manière automatisée par `commitlint` (configuré via `commitlint.config.js` qui étend `@commitlint/config-conventional`).

- **Format général :** `<type>(<scope>): <ticket-Jira> <description>` (le préfixe avec le ticket Jira dans le sujet est optionnel mais recommandé s'il existe).
- **Structure de l'en-tête (Header) :** Max 72 caractères. Commencer par une minuscule, sans majuscule initiale, et sans point final. La description de l'action doit comporter au moins 5 caractères.
- **Types de commits autorisés :**
  - `feat` : Ajout d'une nouvelle fonctionnalité (feature).
  - `fix` : Correction d'un bug (bug fix).
  - `docs` : Modifications de la documentation.
  - `style` : Changements de style et formatage (pas de modification de code logique).
  - `refactor` : Modification du code qui ne corrige pas un bug et n'ajoute pas de fonctionnalité.
  - `perf` : Amélioration des performances.
  - `test` : Ajout ou correction de tests.
  - `build` : Changements affectant le système de build ou les dépendances externes (ex: npm, webpack, pnpm).
  - `ci` : Changements des fichiers de configuration ou scripts d'intégration continue.
  - `chore` : Autres modifications ne touchant pas les fichiers de production ou de test (ex: gestion des versions, configuration de linter).
  - `revert` : Annulation d'un commit précédent.

- **Exemples conformes :**
  ```
  feat(parser): PROJ-123 add ability to parse arrays
  fix: correct minor typos in user validation
  docs(readme): PROJ-456 update installation instructions
  test(auth): add unit test coverage for Functional Guards
  ```

### 9.2. Hooks de Pré-commit (Husky & lint-staged)
L'environnement utilise `husky` et `lint-staged` pour intercepter les validations de commit et garantir l'excellence du code avant qu'il ne quitte le poste local de développement.
- **`pre-commit` :** Déclenche l'utilitaire `lint-staged` via `npx lint-staged` pour traiter les fichiers modifiés et indexés :
  - Formatage instantané : exécution d'`oxfmt` sur tous les fichiers du répertoire `/app` ayant pour extensions `.ts, .js, .tsx, .jsx, .vue, .html, .css, .scss`.
  - Analyse statique et auto-correction : exécution d'`oxlint --fix` sur tous les fichiers TypeScript/JavaScript de `/app`.
  - Formatage des styles : exécution de `stylelint --fix` sur les extensions `.css, .scss` de `/app`.
- **`commit-msg` :** Soumet le message de commit à la validation stricte de `commitlint`. S'il n'est pas conforme, le commit est immédiatement rejeté.

### 9.3. Checklist Pull Request (PR)
Avant de soumettre une Pull Request pour revue :
1. **Description claire :** Fournissez un résumé complet des modifications de comportement introduites et dressez la liste de toutes les zones de l'application impactées.
2. **Liaison JIRA :** Indiquez explicitement les liens vers les tickets Jira associés.
3. **Vérification Locale :** Assurez-vous que l'ensemble du linting, du type-checking et de la couverture de tests passe sans erreur (`pnpm lint`, `pnpm test`) sur votre environnement local avant d'initier la demande de relecture.
