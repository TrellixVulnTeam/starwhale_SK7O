import create, { StateCreator } from 'zustand'
// persist
import { devtools, subscribeWithSelector } from 'zustand/middleware'
import produce from 'immer'
import { v4 as uuid } from 'uuid'
import _ from 'lodash'
// eslint-disable-next-line import/no-cycle
import { ConfigT, SortDirectionsT } from './types'
// eslint-disable-next-line import/no-cycle
import { FilterOperateSelectorValueT } from './filter-operate-selector'

// eslint-disable-next-line prefer-template
const getId = (str: string) => str + '-' + uuid().substring(0, 8)

export interface ITableStateInitState {
    isInit: boolean
    key: string
    setRawConfigs: (obj: Record<string, any>) => void
    getRawConfigs: (state?: ITableState) => typeof rawInitialState
}
export interface IViewState {
    views: ConfigT[]
    defaultView: ConfigT
    setViews: (views: ConfigT[]) => void
    onViewAdd: (view: ConfigT) => void
    onViewUpdate: (view: ConfigT) => void
    getDefaultViewId: () => string
    checkDuplicateViewName: (name: string, viewId: string) => boolean
}
export interface ICurrentViewState {
    currentView: ConfigT
    onCurrentViewSort: (key: string, direction: SortDirectionsT) => void
    onCurrentViewFiltersChange: (filters: FilterOperateSelectorValueT[]) => void
    onCurrentViewColumnsChange: (selectedIds: any[], pinnedIds: any[], sortedIds: any[]) => void
    onCurrentViewColumnsPin: (columnId: string, bool?: boolean) => void
}
export interface IViewInteractiveState {
    viewEditing: ConfigT
    viewModelShow: boolean
    onShowViewModel: (viewModelShow: boolean, viewEditing: ConfigT | null) => void
}
export type ITableState = IViewState &
    ICurrentViewState &
    IViewInteractiveState &
    ITableStateInitState &
    IRowState &
    ICompareState

// , ['zustand/persist', unknown]
export type IStateCreator<T> = StateCreator<
    ITableState,
    [['zustand/subscribeWithSelector', never], ['zustand/devtools', never]],
    [],
    T
>

const rawInitialState: Partial<ITableState> = {
    isInit: false,
    key: 'table',
    views: [],
    defaultView: {},
    currentView: {},
    viewEditing: {},
    viewModelShow: false,
    rowSelectedIds: [],
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createViewSlice: IStateCreator<IViewState> = (set, get, store) => ({
    views: [],
    defaultView: {},
    setViews: (views) =>
        set(
            produce((state) => {
                // eslint-disable-next-line no-param-reassign
                state.views = views
                // eslint-disable-next-line no-param-reassign
                state.currentView = views.find((view) => view.def) || {}
            })
        ),
    onViewAdd: (view) => set({ views: [...get().views, view] }),
    onViewUpdate: (view) => {
        const $oldViewIndex = get().views?.findIndex((v) => v.id === view.id)

        // console.log($oldViewIndex, get().currentView.id, view.id, view.def)
        // create
        if ($oldViewIndex > -1) {
            set(
                produce((state) => {
                    // eslint-disable-next-line no-param-reassign
                    state.views[$oldViewIndex] = view

                    // edit default view and default == current so replace it && view.def === true
                    if (get().currentView?.id === view.id) {
                        // eslint-disable-next-line no-param-reassign
                        state.currentView = view
                    }
                })
            )
        } else {
            const $views = get().views?.map((v) => ({
                ...v,
                def: false,
            }))
            set(
                produce((state) => {
                    const newView = {
                        ...view,
                        def: true,
                        isShow: true,
                        id: getId('view'),
                    }
                    // eslint-disable-next-line no-param-reassign
                    state.views = [...$views, newView]
                    // eslint-disable-next-line no-param-reassign
                    state.currentView = newView
                })
            )
        }
    },
    checkDuplicateViewName: (name: string, viewId: string) => {
        return get()
            .views.filter((view) => view.id !== viewId)
            .some((view) => view.name === name)
    },
    getDefaultViewId: () => get().views?.find((view) => view.def)?.id ?? '',
})

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createCurrentViewSlice: IStateCreator<ICurrentViewState> = (set, get, store) => ({
    currentView: {},
    onCurrentViewFiltersChange: (filters) => set({ currentView: { ...get().currentView, filters } }),
    onCurrentViewColumnsChange: (selectedIds: any[], pinnedIds: any[], sortedIds: any[]) =>
        set({ currentView: { ...get().currentView, selectedIds, pinnedIds, sortedIds } }),
    onCurrentViewColumnsPin: (columnId: string, pined = false) => {
        const { pinnedIds = [], selectedIds = [] } = get().currentView
        const $pinnedIds = new Set(pinnedIds)
        if (pined) {
            $pinnedIds.add(columnId)
        } else {
            $pinnedIds.delete(columnId)
        }
        const sortedMergeSelectedIds = Array.from(selectedIds).sort((v1, v2) => {
            const index1 = $pinnedIds.has(v1) ? 1 : -1
            const index2 = $pinnedIds.has(v2) ? 1 : -1
            return index2 - index1
        })

        set({
            currentView: {
                ...get().currentView,
                pinnedIds: Array.from($pinnedIds),
                selectedIds: sortedMergeSelectedIds,
            },
        })
    },
    onCurrentViewSort: (key, direction) =>
        set({ currentView: { ...get().currentView, sortBy: key, sortDirection: direction } }),
})

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createViewInteractiveSlice: IStateCreator<IViewInteractiveState> = (set, get, store) => ({
    viewEditing: {},
    viewModelShow: false,
    onShowViewModel: (viewModelShow, viewEditing) =>
        set(
            produce((state) => {
                // eslint-disable-next-line no-param-reassign
                state.viewEditing = viewEditing
                // eslint-disable-next-line no-param-reassign
                state.viewModelShow = viewModelShow
            })
        ),
})

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createTableStateInitSlice: IStateCreator<ITableStateInitState> = (set, get, store) => ({
    isInit: false,
    key: 'table',
    setRawConfigs: (obj: Record<string, any>) =>
        set({
            ..._.pick(obj, Object.keys(rawInitialState)),
        }),
    getRawConfigs: (state) => _.pick(state ?? get(), Object.keys(rawInitialState)),
})

export interface IRowState {
    rowSelectedIds: Array<any>
    onSelectMany: (args: any[]) => void
    onSelectNone: () => void
    onSelectOne: (id: any) => void
    onNoSelect: (id: any) => void
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createRowSlice: IStateCreator<IRowState> = (set, get, store) => ({
    rowSelectedIds: [],
    onSelectMany: (incomingRows: any[]) =>
        set({
            rowSelectedIds: [...incomingRows],
        }),
    onSelectNone: () =>
        set({
            rowSelectedIds: [],
        }),
    onNoSelect: (id: any) => {
        const selectedRowIds = new Set(get().rowSelectedIds)
        selectedRowIds.delete(id)
        set({
            rowSelectedIds: Array.from(selectedRowIds),
        })
    },
    onSelectOne: (id: any) => {
        const selectedRowIds = new Set(get().rowSelectedIds)
        if (selectedRowIds.has(id)) {
            selectedRowIds.delete(id)
        } else {
            selectedRowIds.add(id)
        }
        set({
            rowSelectedIds: Array.from(selectedRowIds),
        })
    },
})

export interface ICompareState {
    compare?: {
        comparePinnedKey: any
        compareShowCellChanges: boolean
        compareShowDiffOnly: boolean
    }
    onCompareUpdate: (args: any) => void
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const createCompareSlice: IStateCreator<ICompareState> = (set, get, store) => ({
    onCompareUpdate: (args) =>
        set({
            compare: {
                ...get().compare,
                ...args,
            },
        }),
})

export function createCustomStore(key: string, initState: Partial<ITableState> = {}) {
    let initialized = null
    if (initialized) return initialized
    const name = `table/${key}`
    const useStore = create<ITableState>()(
        subscribeWithSelector(
            devtools(
                // persist(
                (...a) => ({
                    ...createTableStateInitSlice(...a),
                    ...createViewSlice(...a),
                    ...createCurrentViewSlice(...a),
                    ...createViewInteractiveSlice(...a),
                    ...createRowSlice(...a),
                    ...createCompareSlice(...a),
                    ...initState,
                    key: name,
                }),
                //     { name }
                // ),
                { name }
            )
        )
    )
    // eslint-disable-next-line
    // useStore.subscribe(console.log)
    // TODO type define
    // @ts-ignore
    initialized = useStore
    return useStore
}
export type IStore = ReturnType<typeof createCustomStore>

export default createCustomStore

export const useEvaluationStore = createCustomStore('evaluations')
export const useEvaluationCompareStore = createCustomStore('compare', {
    compare: {
        comparePinnedKey: '',
        compareShowCellChanges: true,
        compareShowDiffOnly: false,
    },
})
