@Library('socrata-pipeline-library@9.9.2') _

commonPipeline(
    defaultBuildWorker: 'worker-java-multi-pg13',
    jobName: 'region-coder',
    language: 'scala',
    projects: [
        [
            name: 'region-coder',
            compiled: true,
            deploymentEcosystem: 'ecs',
            paths: [
                dockerBuildContext: 'docker',
            ],
            type: 'service'
        ]
    ],
    teamsChannelWebhookId: 'WORKFLOW_EGRESS_AUTOMATION',
)
